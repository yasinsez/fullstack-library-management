"use client";

import { useAuth } from "../../context/AuthContext";
import { useEffect, useState } from "react";

interface ProfileRoleString {
  name: string;
}
interface ProfileData {
  fullName: string;
  email: string;
  phoneNumber?: string | null;
  roles: Array<string | ProfileRoleString>;
}

const ProfilePage = () => {
  const { user } = useAuth();
  const [profile, setProfile] = useState<ProfileData | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [isEditing, setIsEditing] = useState<boolean>(false);
  const [saving, setSaving] = useState<boolean>(false);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [form, setForm] = useState<{ fullName: string; phoneNumber: string }>({
    fullName: "",
    phoneNumber: "",
  });
  const [showPasswordModal, setShowPasswordModal] = useState(false);
  const [passwordForm, setPasswordForm] = useState({
    currentPassword: "",
    newPassword: "",
    confirmPassword: "",
  });
  const [passwordSaving, setPasswordSaving] = useState(false);
  const [passwordError, setPasswordError] = useState<string | null>(null);
  type Fine = {
    id: number;
    loan?: { book?: { title?: string } };
    amount: number | string;
    reason?: string;
    status?: string | { name?: string };
    issueDate?: string;
  };
  const [fines, setFines] = useState<Fine[]>([]);
  const [loadingFines, setLoadingFines] = useState(false);
  const [payingFineId, setPayingFineId] = useState<number | null>(null);
  type MyReservation = {
    id: number;
    book?: { title?: string };
    priorityNumber?: number;
    status?: string;
  };
  const [reservations, setReservations] = useState<MyReservation[]>([]);
  const [loadingReservations, setLoadingReservations] = useState(false);
  const [actingReservationId, setActingReservationId] = useState<number | null>(null);

  useEffect(() => {
    const fetchProfile = async () => {
      if (!user) {
        // No user yet; keep loading until auth context initializes
        setLoading(true);
        return;
      }

      setLoading(true);
      setError(null);
      try {
        const token =
          typeof window !== "undefined" ? localStorage.getItem("token") : null;
        // Trace the request and token presence (do not log full token)
        console.log("[Profile] Preparing GET /api/profile", {
          hasToken: Boolean(token),
          tokenPreview: token ? token.substring(0, 12) + "..." : null,
        });

        // Use Next.js route to include httpOnly cookie automatically
        const response = await fetch("/api/profile", {
          credentials: "include",
        });
        console.log("[Profile] GET /api/profile response", {
          status: response.status,
        });
        if (!response.ok) {
          const errorText = await response.text();
          throw new Error(errorText || "Failed to load profile");
        }
        const data = await response.json();
        setProfile(data);
        setForm({
          fullName: data?.fullName ?? "",
          phoneNumber: data?.phoneNumber ?? "",
        });
      } catch (err: unknown) {
        console.error("Failed to fetch profile:", err);
        const e = err as {
          response?: { data?: { message?: string } };
          message?: string;
        };
        setError(
          e?.response?.data?.message || e?.message || "Failed to load profile"
        );
      } finally {
        setLoading(false);
      }
    };
    fetchProfile();
  }, [user]);

  useEffect(() => {
    const fetchFines = async () => {
      if (!user) return;
      setLoadingFines(true);
      try {
        const resp = await fetch("/api/fines", {
          cache: "no-store",
          credentials: "include",
        });
        if (resp.ok) {
          const data = await resp.json();
          setFines(Array.isArray(data) ? data : []);
        }
      } finally {
        setLoadingFines(false);
      }
    };
    fetchFines();
  }, [user]);

  useEffect(() => {
    const fetchReservations = async () => {
      if (!user) return;
      setLoadingReservations(true);
      try {
        const resp = await fetch("/api/reservations", {
          cache: "no-store",
          credentials: "include",
        });
        if (resp.ok) {
          setReservations(await resp.json());
        }
      } finally {
        setLoadingReservations(false);
      }
    };
    fetchReservations();
  }, [user]);

  const refreshReservations = async () => {
    setLoadingReservations(true);
    try {
      const resp = await fetch("/api/reservations", { cache: "no-store", credentials: "include" });
      if (resp.ok) setReservations(await resp.json());
    } finally {
      setLoadingReservations(false);
    }
  };

  const cancelReservation = async (id: number) => {
    try {
      setActingReservationId(id);
      const resp = await fetch(`/api/reservations/${id}/cancel`, { method: "POST", credentials: "include" });
      if (!resp.ok && resp.status !== 204) throw new Error(await resp.text());
    } catch (e) {
      console.error("Failed to cancel reservation", e);
    } finally {
      setActingReservationId(null);
      await refreshReservations();
    }
  };

  const borrowReservation = async (id: number) => {
    try {
      setActingReservationId(id);
      const resp = await fetch(`/api/reservations/${id}/borrow`, { method: "POST", credentials: "include" });
      if (!resp.ok) throw new Error(await resp.text());
    } catch (e) {
      console.error("Failed to borrow reservation", e);
    } finally {
      setActingReservationId(null);
      await refreshReservations();
    }
  };

  const isFinePending = (
    status: string | { name?: unknown } | null | undefined
  ): boolean => {
    let s: string | undefined = undefined;
    if (typeof status === "string") {
      s = status;
    } else if (status && typeof status === "object" && "name" in status) {
      const name = (status as { name?: unknown }).name;
      if (typeof name === "string") s = name;
    }
    return typeof s === "string" && s.toUpperCase() === "PENDING";
  };

  const payFine = async (fineId: number) => {
    try {
      setPayingFineId(fineId);
      const resp = await fetch(`/api/fines?id=${fineId}`, {
        method: "PUT",
        credentials: "include",
      });
      if (!resp.ok && resp.status !== 204) throw new Error(await resp.text());
    } catch (e) {
      console.error("Failed to pay fine", e);
    } finally {
      setPayingFineId(null);
      // Always refresh fines to reflect the latest state, even if the PUT errored
      try {
        const refreshed = await fetch("/api/fines", {
          cache: "no-store",
          credentials: "include",
        });
        if (refreshed.ok) setFines(await refreshed.json());
      } catch {
        // ignore refresh errors; UI will remain as-is
      }
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const startEditing = () => {
    setSuccessMessage(null);
    setIsEditing(true);
  };

  const cancelEditing = () => {
    if (profile) {
      setForm({
        fullName: profile.fullName ?? "",
        phoneNumber: profile.phoneNumber ?? "",
      });
    }
    setIsEditing(false);
    setSuccessMessage(null);
  };

  const saveProfile = async () => {
    setSaving(true);
    setError(null);
    setSuccessMessage(null);
    try {
      const payload = {
        fullName: form.fullName,
        phoneNumber: form.phoneNumber || null,
      };
      const resp = await fetch("/api/profile", {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify(payload),
      });
      if (!resp.ok) {
        const errorText = await resp.text();
        throw new Error(errorText || "Failed to update profile");
      }
      const updated = await resp.json();
      setProfile(updated);
      setIsEditing(false);
      setSuccessMessage("Profile updated successfully.");
    } catch (err: unknown) {
      console.error("Failed to update profile:", err);
      const e = err as {
        response?: { data?: { message?: string } };
        message?: string;
      };
      setError(
        e?.response?.data?.message || e?.message || "Failed to update profile"
      );
    } finally {
      setSaving(false);
    }
  };

  const openPasswordModal = () => {
    setPasswordForm({
      currentPassword: "",
      newPassword: "",
      confirmPassword: "",
    });
    setPasswordError(null);
    setSuccessMessage(null);
    setShowPasswordModal(true);
  };

  const closePasswordModal = () => setShowPasswordModal(false);

  const submitPasswordChange = async () => {
    if (!passwordForm.currentPassword || !passwordForm.newPassword) {
      setPasswordError("Please fill all password fields");
      return;
    }
    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      setPasswordError("New passwords do not match");
      return;
    }
    setPasswordSaving(true);
    setPasswordError(null);
    try {
      const resp = await fetch("/api/profile/change-password", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify({
          currentPassword: passwordForm.currentPassword,
          newPassword: passwordForm.newPassword,
        }),
      });
      if (!resp.ok) {
        const text = await resp.text();
        throw new Error(text || "Failed to change password");
      }
      setShowPasswordModal(false);
      setSuccessMessage("Password changed successfully.");
    } catch (err: unknown) {
      const e = err as { message?: string };
      setPasswordError(e.message || "Failed to change password");
    } finally {
      setPasswordSaving(false);
    }
  };

  if (loading) return <p>Loading...</p>;
  if (error)
    return (
      <div className="container mx-auto py-10">
        <h1 className="text-3xl font-bold mb-6">Profile</h1>
        <div className="bg-red-50 text-red-700 border border-red-200 rounded p-4">
          {error}
        </div>
      </div>
    );
  if (!user || !profile) return <p>Loading...</p>;

  return (
    <div className="container mx-auto py-10">
      <h1 className="text-3xl font-bold mb-6 text-gray-900">Profile</h1>
      <div className="bg-white shadow-md rounded-lg p-6 border border-gray-200 text-gray-900">
        {successMessage && (
          <div className="mb-4 bg-green-50 text-green-700 border border-green-200 rounded p-3">
            {successMessage}
          </div>
        )}

        <div className="mb-4">
          <label
            htmlFor="fullName"
            className="block text-sm font-medium text-gray-900 mb-1"
          >
            Full name
          </label>
          {isEditing ? (
            <input
              type="text"
              name="fullName"
              id="fullName"
              value={form.fullName}
              onChange={handleChange}
              className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
              placeholder="Your full name"
            />
          ) : (
            <div>
              <strong className="font-semibold">Name:</strong>{" "}
              {profile.fullName}
            </div>
          )}
        </div>

        <div className="mb-4">
          <label
            htmlFor="email"
            className="block text-sm font-medium text-gray-900 mb-1"
          >
            Email
          </label>
          <div className="text-gray-900">{profile.email}</div>
          <p className="text-xs text-gray-500">Email cannot be changed.</p>
        </div>

        <div className="mb-4">
          <label
            htmlFor="phoneNumber"
            className="block text-sm font-medium text-gray-900 mb-1"
          >
            Phone number
          </label>
          {isEditing ? (
            <input
              type="tel"
              name="phoneNumber"
              id="phoneNumber"
              value={form.phoneNumber}
              onChange={handleChange}
              className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
              placeholder="Optional"
            />
          ) : (
            <div>
              <strong className="font-semibold">Phone:</strong>{" "}
              {profile.phoneNumber || "-"}
            </div>
          )}
        </div>

        <div className="mb-6">
          <strong className="font-semibold">Roles:</strong>{" "}
          {Array.isArray(profile.roles)
            ? profile.roles
                .map((role) => (typeof role === "string" ? role : role?.name))
                .filter(Boolean)
                .join(", ")
            : ""}
        </div>

        {!isEditing ? (
          <button
            onClick={startEditing}
            className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none"
          >
            Edit profile
          </button>
        ) : (
          <div className="flex gap-3">
            <button
              onClick={saveProfile}
              disabled={saving || !form.fullName.trim()}
              className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-green-600 hover:bg-green-700 disabled:opacity-50 focus:outline-none"
            >
              {saving ? "Saving..." : "Save changes"}
            </button>
            <button
              onClick={cancelEditing}
              disabled={saving}
              className="inline-flex items-center px-4 py-2 border text-sm font-medium rounded-md shadow-sm bg-white text-gray-700 border-gray-300 hover:bg-gray-50 focus:outline-none"
            >
              Cancel
            </button>
          </div>
        )}
        <div className="mt-4">
          <button
            onClick={openPasswordModal}
            className="inline-flex items-center px-4 py-2 border text-sm font-medium rounded-md shadow-sm bg-white text-gray-800 border-gray-300 hover:bg-gray-50 focus:outline-none"
          >
            Change password
          </button>
        </div>
      </div>
      <div className="mt-8 bg-white shadow-md rounded-lg p-6 border border-gray-200 text-gray-900">
        <h2 className="text-2xl font-bold mb-4">My Fines</h2>
        {loadingFines ? (
          <div>Loading fines...</div>
        ) : fines.length === 0 ? (
          <div>No fines.</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
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
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Issued
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {fines.map((fine) => (
                  <tr key={fine.id}>
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
                      <span className="mr-3">
                        {typeof fine.status === "string"
                          ? fine.status
                          : fine.status?.name}
                      </span>
                      {isFinePending(fine.status) && (
                        <button
                          onClick={() => payFine(fine.id)}
                          disabled={payingFineId === fine.id}
                          className="px-3 py-1 rounded bg-green-600 text-white hover:bg-green-700 disabled:opacity-50"
                        >
                          {payingFineId === fine.id ? "Paying..." : "Pay"}
                        </button>
                      )}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {fine.issueDate
                        ? new Date(fine.issueDate).toLocaleDateString()
                        : "-"}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
      <div className="mt-8 bg-white shadow-md rounded-lg p-6 border border-gray-200 text-gray-900">
        <h2 className="text-2xl font-bold mb-4">My Reservations</h2>
        {loadingReservations ? (
          <div>Loading reservations...</div>
        ) : reservations.length === 0 ? (
          <div>No reservations.</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Book
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Queue
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
                      {r?.priorityNumber ?? "-"}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {r?.status}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-right text-sm text-gray-900">
                      <div className="space-x-2">
                        {typeof r.status === "string" && r.status.toUpperCase() === "READY_FOR_PICKUP" && (
                          <button
                            onClick={() => borrowReservation(r.id)}
                            disabled={actingReservationId === r.id}
                            className="px-3 py-1 rounded bg-green-600 text-white disabled:opacity-50"
                          >
                            {actingReservationId === r.id ? "Processing..." : "Borrow"}
                          </button>
                        )}
                        {typeof r.status === "string" && ["PENDING", "READY_FOR_PICKUP"].includes(r.status.toUpperCase()) && (
                          <button
                            onClick={() => cancelReservation(r.id)}
                            disabled={actingReservationId === r.id}
                            className="px-3 py-1 rounded bg-yellow-600 text-white disabled:opacity-50"
                          >
                            {actingReservationId === r.id ? "Processing..." : "Cancel"}
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
      {showPasswordModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
          <div className="bg-white rounded-lg shadow-lg w-full max-w-md p-6 text-gray-900">
            <h2 className="text-xl font-semibold mb-4">Change password</h2>
            {passwordError && (
              <div className="mb-3 bg-red-50 text-red-700 border border-red-200 rounded p-3">
                {passwordError}
              </div>
            )}
            <div className="space-y-4">
              <div>
                <label
                  htmlFor="currentPassword"
                  className="block text-sm font-medium text-gray-900 mb-1"
                >
                  Current password
                </label>
                <input
                  id="currentPassword"
                  type="password"
                  className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
                  value={passwordForm.currentPassword}
                  onChange={(e) =>
                    setPasswordForm((p) => ({
                      ...p,
                      currentPassword: e.target.value,
                    }))
                  }
                />
              </div>
              <div>
                <label
                  htmlFor="newPassword"
                  className="block text-sm font-medium text-gray-900 mb-1"
                >
                  New password
                </label>
                <input
                  id="newPassword"
                  type="password"
                  className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
                  value={passwordForm.newPassword}
                  onChange={(e) =>
                    setPasswordForm((p) => ({
                      ...p,
                      newPassword: e.target.value,
                    }))
                  }
                />
              </div>
              <div>
                <label
                  htmlFor="confirmPassword"
                  className="block text-sm font-medium text-gray-900 mb-1"
                >
                  Confirm new password
                </label>
                <input
                  id="confirmPassword"
                  type="password"
                  className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
                  value={passwordForm.confirmPassword}
                  onChange={(e) =>
                    setPasswordForm((p) => ({
                      ...p,
                      confirmPassword: e.target.value,
                    }))
                  }
                />
              </div>
            </div>
            <div className="mt-6 flex justify-end gap-3">
              <button
                onClick={closePasswordModal}
                className="px-4 py-2 text-sm rounded-md border border-gray-300 bg:white text-gray-800 hover:bg-gray-50"
              >
                Cancel
              </button>
              <button
                onClick={submitPasswordChange}
                disabled={passwordSaving}
                className="px-4 py-2 text-sm rounded-md bg-indigo-600 text-white hover:bg-indigo-700 disabled:opacity-50"
              >
                {passwordSaving ? "Saving..." : "Change password"}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ProfilePage;
