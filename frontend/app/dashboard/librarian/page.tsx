"use client";

import { useAuth } from "../../../context/AuthContext";
import BookManagementTable from "../../../components/dashboard/BookManagementTable";
import AuthorManagementTable from "../../../components/dashboard/AuthorManagementTable";
import Link from "next/link";
import { useEffect, useState } from "react";
import DeleteConfirmationModal from "../../../components/dashboard/DeleteConfirmationModal";

const LibrarianDashboardPage = () => {
  const { user } = useAuth();

  if (!user || !user.roles.includes("LIBRARIAN")) {
    return (
      <div className="text-center py-10">
        <p>You must be a logged-in librarian to view this page.</p>
      </div>
    );
  }

  type ReservationRow = {
    id: number;
    book?: { title?: string };
    member?: { fullName?: string; user?: { fullName?: string } };
    priorityNumber?: number;
    status?: string;
  };

  const ReservationsTable = () => {
    const [reservations, setReservations] = useState<ReservationRow[]>([]);
    const [deleteTarget, setDeleteTarget] = useState<{
      id: number;
      itemName: string;
    } | null>(null);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
      const fetchReservations = async () => {
        setLoading(true);
        try {
          const resp = await fetch("/api/reservations?scope=admin", {
            cache: "no-store",
          });
          if (resp.ok) {
            setReservations(await resp.json());
          }
        } finally {
          setLoading(false);
        }
      };
      fetchReservations();
    }, []);

    const refresh = async () => {
      setLoading(true);
      try {
        const resp = await fetch("/api/reservations?scope=admin", {
          cache: "no-store",
        });
        if (resp.ok) setReservations(await resp.json());
      } finally {
        setLoading(false);
      }
    };

    const cancelReservation = async (id: number) => {
      const resp = await fetch(`/api/reservations/${id}`, { method: "DELETE" });
      if (resp.ok || resp.status === 204) await refresh();
    };
    const hardDeleteReservation = async (id: number) => {
      const resp = await fetch(`/api/reservations/${id}?hard=true`, {
        method: "DELETE",
      });
      if (resp.ok || resp.status === 204) await refresh();
    };

    const promoteReservation = async (id: number) => {
      const resp = await fetch(`/api/reservations/${id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ status: "READY_FOR_PICKUP" }),
      });
      if (resp.ok || resp.status === 204) await refresh();
    };

    if (loading) return <div className="p-4">Loading reservations...</div>;

    return (
      <>
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Book
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Member
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Queued
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Status
                </th>
                <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {reservations.map((r) => (
                <tr key={r.id}>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                    {r?.book?.title || "-"}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                    {r?.member?.fullName || r?.member?.user?.fullName || "-"}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                    <div className="flex items-center gap-2">
                      <input
                        type="number"
                        defaultValue={r?.priorityNumber ?? 0}
                        min={1}
                        className="w-16 border rounded px-2 py-1"
                        onBlur={async (e) => {
                          const val = parseInt(e.target.value, 10);
                          if (!Number.isNaN(val) && val > 0) {
                            await fetch(`/api/reservations/${r.id}/priority`, {
                              method: "PUT",
                              headers: { "Content-Type": "application/json" },
                              body: JSON.stringify({ priority: val }),
                            });
                            await refresh();
                          }
                        }}
                      />
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                    {r?.status}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-right text-sm text-gray-900">
                    <div className="space-x-2">
                      <button
                        onClick={() => promoteReservation(r.id)}
                        className="px-3 py-1 rounded bg-indigo-600 text-white"
                      >
                        Mark Ready
                      </button>
                      <button
                        onClick={() => cancelReservation(r.id)}
                        className="px-3 py-1 rounded bg-yellow-600 text-white"
                      >
                        Cancel
                      </button>
                      <button
                        onClick={() =>
                          setDeleteTarget({
                            id: r.id,
                            itemName: r?.book?.title || "reservation",
                          })
                        }
                        className="px-3 py-1 rounded bg-red-600 text-white"
                      >
                        Delete
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <DeleteConfirmationModal
          isOpen={Boolean(deleteTarget)}
          onClose={() => setDeleteTarget(null)}
          onConfirm={async () => {
            if (deleteTarget) {
              await hardDeleteReservation(deleteTarget.id);
              setDeleteTarget(null);
            }
          }}
          itemName={deleteTarget?.itemName || "reservation"}
        />
      </>
    );
  };

  type ActiveLoan = {
    id: number;
    checkoutDate: string;
    dueDate: string;
    member?: { user?: { fullName?: string }; fullName?: string };
    bookCopy?: { book?: { title?: string } };
    book?: { title?: string };
  };

  const LibrarianActiveLoansTable = () => {
    const [loans, setLoans] = useState<ActiveLoan[]>([]);
    const [loading, setLoading] = useState(false);
    const [editingId, setEditingId] = useState<number | null>(null);
    const [dueDateInput, setDueDateInput] = useState<string>("");

    useEffect(() => {
      const fetchLoans = async () => {
        setLoading(true);
        try {
          const resp = await fetch("/api/loans?scope=admin-active", {
            cache: "no-store",
          });
          if (resp.ok) {
            const data = await resp.json();
            setLoans(Array.isArray(data) ? data : []);
          }
        } finally {
          setLoading(false);
        }
      };
      fetchLoans();
    }, []);

    const refresh = async () => {
      setLoading(true);
      try {
        const resp = await fetch("/api/loans?scope=admin-active", {
          cache: "no-store",
        });
        if (resp.ok) setLoans(await resp.json());
      } finally {
        setLoading(false);
      }
    };

    const startEdit = (loan: ActiveLoan) => {
      setEditingId(loan.id);
      setDueDateInput(loan.dueDate);
    };

    const saveEdit = async (loanId: number) => {
      const resp = await fetch(`/api/loans/${loanId}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ dueDate: dueDateInput }),
      });
      if (resp.ok) {
        setEditingId(null);
        setDueDateInput("");
        await refresh();
      }
    };

    const cancelEdit = () => {
      setEditingId(null);
      setDueDateInput("");
    };

    const returnLoan = async (loanId: number) => {
      const resp = await fetch(`/api/loans/${loanId}/return`, {
        method: "PUT",
      });
      if (resp.ok) await refresh();
    };

    if (loading) return <div className="p-4">Loading loans...</div>;

    return (
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Book
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Borrower
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Checkout
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Due
              </th>
              <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                Actions
              </th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {loans.map((loan) => (
              <tr key={loan.id}>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {loan?.bookCopy?.book?.title || loan?.book?.title || "-"}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {loan?.member?.user?.fullName ||
                    loan?.member?.fullName ||
                    "-"}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {loan.checkoutDate}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {editingId === loan.id ? (
                    <input
                      type="date"
                      value={dueDateInput}
                      onChange={(e) => setDueDateInput(e.target.value)}
                      className="border rounded px-2 py-1"
                    />
                  ) : (
                    loan.dueDate
                  )}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-right text-sm text-gray-900">
                  {editingId === loan.id ? (
                    <div className="space-x-2">
                      <button
                        onClick={() => saveEdit(loan.id)}
                        className="px-3 py-1 rounded bg-green-600 text-white"
                      >
                        Save
                      </button>
                      <button
                        onClick={cancelEdit}
                        className="px-3 py-1 rounded bg-gray-300 text-gray-800"
                      >
                        Cancel
                      </button>
                    </div>
                  ) : (
                    <div className="space-x-2">
                      <button
                        onClick={() => startEdit(loan)}
                        className="px-3 py-1 rounded bg-indigo-600 text-white"
                      >
                        Edit
                      </button>
                      <button
                        onClick={() => returnLoan(loan.id)}
                        className="px-3 py-1 rounded bg-red-600 text-white"
                      >
                        Mark Returned
                      </button>
                    </div>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    );
  };

  const LibrarianFinesTable = () => {
    type Fine = {
      id: number;
      member?: { fullName?: string; user?: { fullName?: string } };
      loan?: { book?: { title?: string } };
      amount: number | string;
      reason?: string;
      status?: string;
    };
    const [fines, setFines] = useState<Fine[]>([]);
    const [loadingFines, setLoadingFines] = useState(false);

    useEffect(() => {
      const fetchFines = async () => {
        setLoadingFines(true);
        try {
          const resp = await fetch("/api/fines?scope=admin", {
            cache: "no-store",
          });
          if (resp.ok) {
            const data: Fine[] = await resp.json();
            setFines(Array.isArray(data) ? data : []);
          }
        } finally {
          setLoadingFines(false);
        }
      };
      fetchFines();
    }, []);

    const markFinePaid = async (fineId: number) => {
      try {
        const resp = await fetch(`/api/fines?id=${fineId}`, { method: "PUT" });
        if (resp.ok || resp.status === 204) {
          // refresh fines
          const r = await fetch("/api/fines?scope=admin", {
            cache: "no-store",
          });
          if (r.ok) setFines(await r.json());
        }
      } catch {}
    };

    if (loadingFines) return <div className="p-4">Loading fines...</div>;

    return (
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Member
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Book
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Amount
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Reason
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Status
              </th>
              <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                Actions
              </th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {fines.map((fine) => (
              <tr key={fine.id}>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {fine?.member?.fullName ||
                    fine?.member?.user?.fullName ||
                    "-"}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {fine?.loan?.book?.title || "-"}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {fine.amount}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {fine.reason}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {fine.status}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-right text-sm text-gray-900">
                  {typeof fine.status === "string" &&
                  fine.status.toUpperCase() === "PENDING" ? (
                    <button
                      onClick={() => markFinePaid(fine.id)}
                      className="px-3 py-1 rounded bg-green-600 text-white hover:bg-green-700"
                    >
                      Mark Paid
                    </button>
                  ) : null}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    );
  };

  return (
    <div className="container mx-auto py-10 space-y-12">
      <h1 className="text-3xl font-bold">Librarian Dashboard</h1>
      <p className="mb-2">Welcome, {user.fullName}!</p>

      <div className="space-y-12">
        <div className="bg-white shadow-md rounded-lg overflow-hidden">
          <BookManagementTable />
        </div>

        <div className="bg-white shadow-md rounded-lg overflow-hidden">
          <AuthorManagementTable />
        </div>

        <div className="bg-white shadow-md rounded-lg overflow-hidden">
          <div className="flex justify-between items-center mb-4 px-6 py-4">
            <h2 className="text-2xl font-bold text-gray-800">Active Loans</h2>
            <Link href="#" className="text-sm text-indigo-600 hover:underline">
              Refresh
            </Link>
          </div>
          <LibrarianActiveLoansTable />
        </div>

        {/* Fines Section */}
        <div className="bg-white shadow-md rounded-lg overflow-hidden">
          <div className="flex justify-between items-center mb-4 px-6 py-4">
            <h2 className="text-2xl font-bold text-gray-800">Fines</h2>
          </div>
          <LibrarianFinesTable />
        </div>

        <div className="bg-white shadow-md rounded-lg overflow-hidden">
          <div className="flex justify-between items-center mb-4 px-6 py-4">
            <h2 className="text-2xl font-bold text-gray-800">Reservations</h2>
          </div>
          <ReservationsTable />
        </div>
      </div>
    </div>
  );
};

export default LibrarianDashboardPage;
