"use client";

import { useState, useEffect } from "react";
import CreateUserModal from "./CreateUserModal";
import DeleteConfirmationModal from "./DeleteConfirmationModal";
import UpdateUserModal from "./UpdateUserModal";

export type Role = {
  id: number;
  name: string;
  description: string;
};

export type UserRow = {
  id: number;
  fullName: string;
  email: string;
  phoneNumber: string;
  active: boolean;
  status: string;
  roles: Role[];
};

const UserManagementTable = () => {
  const [users, setUsers] = useState<UserRow[]>([]);
  const [loading, setLoading] = useState(true);
  const [isCreateModalOpen, setCreateModalOpen] = useState(false);
  const [isUpdateModalOpen, setUpdateModalOpen] = useState(false);
  const [isDeleteModalOpen, setDeleteModalOpen] = useState(false);
  const [selectedUser, setSelectedUser] = useState<UserRow | null>(null);
  const [currentPage, setCurrentPage] = useState(1);
  const PAGE_SIZE = 5;

  useEffect(() => {
    const fetchUsers = async () => {
      setLoading(true);
      try {
        const resp = await fetch("/api/users", {
          cache: "no-store",
          credentials: "include",
        });
        if (!resp.ok) {
          throw new Error(await resp.text());
        }
        const data = await resp.json();
        setUsers(Array.isArray(data) ? data : []);
      } catch (error) {
        console.error("Failed to fetch users:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchUsers();
  }, []);

  // Keep page within bounds when list size changes
  useEffect(() => {
    const newTotalPages = Math.max(1, Math.ceil(users.length / PAGE_SIZE));
    setCurrentPage((prev) => Math.min(prev, newTotalPages));
  }, [users]);

  const handleCreate = async (
    userData: Omit<UserRow, "id" | "roles"> & {
      password: string;
      username: string;
      phoneNumber: string;
      active: boolean;
      status: string;
      roleIds: number[];
    }
  ) => {
    try {
      const resp = await fetch("/api/users", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify(userData),
      });
      if (!resp.ok) throw new Error(await resp.text());
      const created = await resp.json();
      setUsers([...users, created]);
    } catch (error) {
      console.error("Failed to create user:", error);
    }
  };

  const handleUpdate = async (
    id: number,
    userData: Partial<UserRow> & { roleIds?: number[] }
  ) => {
    try {
      const resp = await fetch(`/api/users/${id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify(userData),
      });
      if (!resp.ok) throw new Error(await resp.text());
      const updated = await resp.json();
      setUsers(users.map((user) => (user.id === id ? updated : user)));
      setUpdateModalOpen(false);
      setSelectedUser(null);
    } catch (error) {
      console.error("Failed to update user:", error);
    }
  };

  const handleDelete = async (id: number) => {
    try {
      const resp = await fetch(`/api/users/${id}`, {
        method: "DELETE",
        credentials: "include",
      });
      if (!resp.ok && resp.status !== 204) throw new Error(await resp.text());
      setUsers(users.filter((user) => user.id !== id));
      setDeleteModalOpen(false);
      setSelectedUser(null);
    } catch (error) {
      console.error("Failed to delete user:", error);
    }
  };

  const openUpdateModal = (user: UserRow) => {
    setSelectedUser(user);
    setUpdateModalOpen(true);
  };

  const openDeleteModal = (user: UserRow) => {
    setSelectedUser(user);
    setDeleteModalOpen(true);
  };

  if (loading) {
    return (
      <div className="text-center py-10">
        <p>Loading users...</p>
      </div>
    );
  }

  return (
    <div className="bg-white shadow-md rounded-lg overflow-hidden">
      <div className="flex justify-between items-center mb-4 px-6 py-4">
        <h2 className="text-2xl font-bold text-gray-800">User Management</h2>
        <button
          onClick={() => setCreateModalOpen(true)}
          className="px-4 py-2 rounded-md font-semibold text-sm bg-green-500 text-white hover:bg-green-600"
        >
          Add New User
        </button>
      </div>
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Name
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Email
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Roles
              </th>
              <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                Actions
              </th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {users
              .slice(
                (currentPage - 1) * PAGE_SIZE,
                (currentPage - 1) * PAGE_SIZE + PAGE_SIZE
              )
              .map((user) => (
                <tr key={user.id}>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                    {user.fullName}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {user.email}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {user.roles.map((role) => role.name).join(", ")}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                    <div className="flex justify-end items-center space-x-2">
                      <button
                        onClick={() => openUpdateModal(user)}
                        className="px-4 py-2 rounded-md font-semibold text-sm bg-indigo-500 text-white hover:bg-indigo-600"
                      >
                        Edit
                      </button>
                      <button
                        onClick={() => openDeleteModal(user)}
                        className="px-4 py-2 rounded-md font-semibold text-sm bg-red-500 text-white hover:bg-red-600"
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
      <div className="flex items-center justify-between px-6 py-3 border-t">
        <button
          onClick={() => setCurrentPage((p) => Math.max(1, p - 1))}
          disabled={currentPage === 1}
          className={`px-3 py-1 rounded border ${
            currentPage === 1
              ? "text-gray-400 border-gray-200 cursor-not-allowed"
              : "text-gray-700 hover:bg-gray-50"
          }`}
        >
          Previous
        </button>
        <span className="text-sm text-gray-600">
          Page {currentPage} of{" "}
          {Math.max(1, Math.ceil(users.length / PAGE_SIZE))}
        </span>
        <button
          onClick={() =>
            setCurrentPage((p) =>
              Math.min(Math.max(1, Math.ceil(users.length / PAGE_SIZE)), p + 1)
            )
          }
          disabled={
            currentPage >= Math.max(1, Math.ceil(users.length / PAGE_SIZE))
          }
          className={`px-3 py-1 rounded border ${
            currentPage >= Math.max(1, Math.ceil(users.length / PAGE_SIZE))
              ? "text-gray-400 border-gray-200 cursor-not-allowed"
              : "text-gray-700 hover:bg-gray-50"
          }`}
        >
          Next
        </button>
      </div>
      <CreateUserModal
        isOpen={isCreateModalOpen}
        onClose={() => setCreateModalOpen(false)}
        onCreate={handleCreate}
      />
      <UpdateUserModal
        isOpen={isUpdateModalOpen}
        onClose={() => {
          setUpdateModalOpen(false);
          setSelectedUser(null);
        }}
        onUpdate={handleUpdate}
        user={selectedUser}
      />
      <DeleteConfirmationModal
        isOpen={isDeleteModalOpen}
        onClose={() => {
          setDeleteModalOpen(false);
          setSelectedUser(null);
        }}
        onConfirm={() => {
          if (selectedUser) {
            handleDelete(selectedUser.id);
          }
        }}
        itemName={selectedUser ? selectedUser.fullName : ""}
      />
    </div>
  );
};

export default UserManagementTable;
