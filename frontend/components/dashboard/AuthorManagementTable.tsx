"use client";

import { useState, useEffect } from "react";
import { apiClient } from "../../lib/apiClient";
import CreateAuthorModal from "./CreateAuthorModal";
import UpdateAuthorModal from "./UpdateAuthorModal";
import DeleteConfirmationModal from "./DeleteConfirmationModal";

export type Author = {
  id: number;
  name: string;
  biography: string;
  dateOfBirth: string;
};

const AuthorManagementTable = () => {
  const [authors, setAuthors] = useState<Author[]>([]);
  const [loading, setLoading] = useState(true);
  const [isCreateModalOpen, setCreateModalOpen] = useState(false);
  const [isUpdateModalOpen, setUpdateModalOpen] = useState(false);
  const [isDeleteModalOpen, setDeleteModalOpen] = useState(false);
  const [selectedAuthor, setSelectedAuthor] = useState<Author | null>(null);
  const [currentPage, setCurrentPage] = useState(1);
  const PAGE_SIZE = 5;

  useEffect(() => {
    const fetchAuthors = async () => {
      setLoading(true);
      try {
        const resp = await fetch("/api/authors", { cache: "no-store" });
        if (resp.ok) {
          setAuthors(await resp.json());
        }
      } catch (error) {
        console.error("Failed to fetch authors:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchAuthors();
  }, []);

  // Maintain page bounds on list size changes
  useEffect(() => {
    const newTotalPages = Math.max(1, Math.ceil(authors.length / PAGE_SIZE));
    setCurrentPage((prev) => Math.min(prev, newTotalPages));
  }, [authors]);

  const handleCreate = async (authorData: Partial<Author>) => {
    try {
      const response = await fetch("/api/authors", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(authorData),
      });
      if (!response.ok) throw new Error(await response.text());
      setAuthors([...authors, await response.json()]);
      setCreateModalOpen(false);
    } catch (error) {
      console.error("Failed to create author:", error);
    }
  };

  const handleUpdate = async (id: number, authorData: Partial<Author>) => {
    try {
      const response = await fetch(`/api/authors/${id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(authorData),
      });
      if (!response.ok) throw new Error(await response.text());
      const updated = await response.json();
      setAuthors(
        authors.map((author) => (author.id === id ? updated : author))
      );
      setUpdateModalOpen(false);
      setSelectedAuthor(null);
    } catch (error) {
      console.error("Failed to update author:", error);
    }
  };

  const handleDelete = async (id: number) => {
    try {
      const resp = await fetch(`/api/authors/${id}`, { method: "DELETE" });
      if (!resp.ok) throw new Error(await resp.text());
      setAuthors(authors.filter((author) => author.id !== id));
      setDeleteModalOpen(false);
      setSelectedAuthor(null);
    } catch (error) {
      console.error("Failed to delete author:", error);
    }
  };

  const openUpdateModal = (author: Author) => {
    setSelectedAuthor(author);
    setUpdateModalOpen(true);
  };

  const openDeleteModal = (author: Author) => {
    setSelectedAuthor(author);
    setDeleteModalOpen(true);
  };

  if (loading) {
    return (
      <div className="text-center py-10">
        <p>Loading authors...</p>
      </div>
    );
  }

  return (
    <div className="bg-white shadow-md rounded-lg overflow-hidden">
      <div className="flex justify-between items-center mb-4 px-6 py-4">
        <h2 className="text-2xl font-bold text-gray-800">Author Management</h2>
        <button
          onClick={() => setCreateModalOpen(true)}
          className="px-4 py-2 rounded-md font-semibold text-sm bg-green-500 text-white hover:bg-green-600"
        >
          Add New Author
        </button>
      </div>
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Name
              </th>
              <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                Actions
              </th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {authors
              .slice(
                (currentPage - 1) * PAGE_SIZE,
                (currentPage - 1) * PAGE_SIZE + PAGE_SIZE
              )
              .map((author) => (
                <tr key={author.id}>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                    {author.name}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                    <div className="flex justify-end items-center space-x-2">
                      <button
                        onClick={() => openUpdateModal(author)}
                        className="px-4 py-2 rounded-md font-semibold text-sm bg-indigo-500 text-white hover:bg-indigo-600"
                      >
                        Edit
                      </button>
                      <button
                        onClick={() => openDeleteModal(author)}
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
          {Math.max(1, Math.ceil(authors.length / PAGE_SIZE))}
        </span>
        <button
          onClick={() =>
            setCurrentPage((p) =>
              Math.min(
                Math.max(1, Math.ceil(authors.length / PAGE_SIZE)),
                p + 1
              )
            )
          }
          disabled={
            currentPage >= Math.max(1, Math.ceil(authors.length / PAGE_SIZE))
          }
          className={`px-3 py-1 rounded border ${
            currentPage >= Math.max(1, Math.ceil(authors.length / PAGE_SIZE))
              ? "text-gray-400 border-gray-200 cursor-not-allowed"
              : "text-gray-700 hover:bg-gray-50"
          }`}
        >
          Next
        </button>
      </div>
      <CreateAuthorModal
        isOpen={isCreateModalOpen}
        onClose={() => setCreateModalOpen(false)}
        onCreate={handleCreate}
      />
      <UpdateAuthorModal
        isOpen={isUpdateModalOpen}
        onClose={() => {
          setUpdateModalOpen(false);
          setSelectedAuthor(null);
        }}
        onUpdate={handleUpdate}
        author={selectedAuthor}
      />
      <DeleteConfirmationModal
        isOpen={isDeleteModalOpen}
        onClose={() => {
          setDeleteModalOpen(false);
          setSelectedAuthor(null);
        }}
        onConfirm={() => {
          if (selectedAuthor) {
            handleDelete(selectedAuthor.id);
          }
        }}
        itemName={selectedAuthor ? selectedAuthor.name : ""}
      />
    </div>
  );
};

export default AuthorManagementTable;
