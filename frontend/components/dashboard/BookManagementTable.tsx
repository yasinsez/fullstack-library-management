"use client";

import { useState, useEffect } from "react";
import { apiClient } from "../../lib/apiClient";
import NewCreateBookModal from "./CreateBookModal";
import UpdateBookModal from "./UpdateBookModal";
import DeleteConfirmationModal from "./DeleteConfirmationModal";

export type Author = {
  id: number;
  name: string;
};

export type Publisher = {
  id: number;
  name: string;
};

export type Category = {
  id: number;
  name: string;
};

export type Book = {
  id: number;
  title: string;
  authors: Author[];
  publisher: Publisher;
  category: Category;
  isbn: string;
  quantity: number; // This will be derived or fetched separately
  publicationYear?: number;
  description?: string;
  coverImage?: string;
  pages?: number;
};

const BookManagementTable = () => {
  const [books, setBooks] = useState<Book[]>([]);
  const [loading, setLoading] = useState(true);
  const [isCreateModalOpen, setCreateModalOpen] = useState(false);
  const [isUpdateModalOpen, setUpdateModalOpen] = useState(false);
  const [isDeleteModalOpen, setDeleteModalOpen] = useState(false);
  const [selectedBook, setSelectedBook] = useState<Book | null>(null);
  const [currentPage, setCurrentPage] = useState(1);
  const PAGE_SIZE = 5;

  const fetchBooks = async () => {
    setLoading(true);
    try {
      const response = await fetch("/api/books", { cache: "no-store" });
      if (!response.ok) throw new Error(await response.text());
      const data = await response.json();
      type ApiBook = {
        id: number;
        title: string;
        authors?: { id: number; name: string }[];
        copies?: unknown[];
        isbn: string;
        publicationYear?: number;
        description?: string;
        coverImage?: string;
        pages?: number;
        publisher?: { id: number; name: string } | string;
        category?: { id: number; name: string };
        stock?: number;
      };
      const adaptedBooks = (data as ApiBook[]).map((book) => {
        const publisherObj =
          typeof book.publisher === "string"
            ? { id: 0, name: book.publisher || "Unknown" }
            : book.publisher ?? { id: 0, name: "Unknown" };
        const categoryObj = book.category ?? { id: 0, name: "Unknown" };
        return {
          id: book.id,
          title: book.title,
          authors: Array.isArray(book.authors) ? book.authors : [],
          publisher: publisherObj,
          category: categoryObj,
          isbn: book.isbn,
          quantity:
            typeof book.stock === "number"
              ? book.stock
              : Array.isArray(book.copies)
              ? book.copies.length
              : 0,
          publicationYear: book.publicationYear,
          description: book.description,
          coverImage: book.coverImage,
          pages: book.pages,
        } as Book;
      });
      setBooks(adaptedBooks);
    } catch (error) {
      console.error("Failed to fetch books:", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchBooks();
  }, []);

  // Ensure current page stays within bounds when data changes
  useEffect(() => {
    const newTotalPages = Math.max(1, Math.ceil(books.length / PAGE_SIZE));
    setCurrentPage((prev) => Math.min(prev, newTotalPages));
  }, [books]);

  const handleCreate = async (bookData: {
    title: string;
    isbn: string;
    publicationYear: number;
    description: string;
    coverImage: string;
    pages: number;
    authorIds: number[];
    publisherId: number;
    categoryId: number;
    stock: number;
  }) => {
    try {
      const newBookData = {
        ...bookData,
        authorIds: bookData.authorIds,
        publisherId: bookData.publisherId,
        categoryId: bookData.categoryId,
        stock: bookData.stock,
      };
      console.log("Creating book with data:", newBookData);
      const resp = await fetch("/api/books", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(newBookData),
      });
      if (!resp.ok) throw new Error(await resp.text());
      const created = await resp.json();
      const newBook = {
        ...created,
        authors: created.authors || [],
        quantity:
          typeof created.stock === "number"
            ? created.stock
            : Array.isArray(created.copies)
            ? created.copies.length
            : 0,
      };
      setBooks([...books, newBook]);
      setCreateModalOpen(false);
    } catch (error) {
      const message = error instanceof Error ? error.message : String(error);
      console.error("Failed to create book:", message);
      alert(message);
    }
  };

  const handleUpdate = async (
    id: number,
    bookData: Partial<
      Omit<Book, "id" | "quantity" | "authors"> & {
        authorIds: number[];
        publisherId: number;
        categoryId: number;
        stock: number;
      }
    >
  ) => {
    try {
      const resp = await fetch(`/api/books/${id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(bookData),
      });
      if (!resp.ok) throw new Error(await resp.text());
      const updatedData = await resp.json();
      const updatedBook = {
        ...updatedData,
        authors: updatedData.authors || [],
        quantity:
          typeof updatedData.stock === "number"
            ? updatedData.stock
            : Array.isArray(updatedData.copies)
            ? updatedData.copies.length
            : 0,
      };
      setBooks(books.map((book) => (book.id === id ? updatedBook : book)));
      setUpdateModalOpen(false);
      setSelectedBook(null);
      await fetchBooks();
    } catch (error) {
      const message = error instanceof Error ? error.message : String(error);
      console.error("Failed to update book:", message);
      alert(message);
    }
  };

  const handleDelete = async (id: number) => {
    try {
      const resp = await fetch(`/api/books/${id}`, { method: "DELETE" });
      if (!resp.ok) throw new Error(await resp.text());
      setBooks(books.filter((book) => book.id !== id));
      setDeleteModalOpen(false);
      setSelectedBook(null);
    } catch (error) {
      const message = error instanceof Error ? error.message : String(error);
      console.error("Failed to delete book:", message);
      alert(message);
    }
  };

  const openUpdateModal = (book: Book) => {
    setSelectedBook(book);
    setUpdateModalOpen(true);
  };

  const openDeleteModal = (book: Book) => {
    setSelectedBook(book);
    setDeleteModalOpen(true);
  };

  if (loading) {
    return (
      <div className="text-center py-10">
        <p>Loading books...</p>
      </div>
    );
  }

  return (
    <div className="bg-white shadow-md rounded-lg overflow-hidden">
      <div className="flex justify-between items-center mb-4 px-6 py-4">
        <h2 className="text-2xl font-bold text-gray-800">Book Management</h2>
        <button
          onClick={() => setCreateModalOpen(true)}
          className="px-4 py-2 rounded-md font-semibold text-sm bg-green-500 text-white hover:bg-green-600"
        >
          Add New Book
        </button>
      </div>
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Title
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Author
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                ISBN
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Stock
              </th>
              <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                Actions
              </th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {books
              .slice(
                (currentPage - 1) * PAGE_SIZE,
                (currentPage - 1) * PAGE_SIZE + PAGE_SIZE
              )
              .map((book) => (
                <tr key={book.id}>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                    {book.title}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {Array.isArray(book.authors) && book.authors.length > 0
                      ? book.authors.map((author) => author.name).join(", ")
                      : "-"}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {book.isbn}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {book.quantity}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                    <div className="flex justify-end items-center space-x-2">
                      <button
                        onClick={() => openUpdateModal(book)}
                        className="px-4 py-2 rounded-md font-semibold text-sm bg-indigo-500 text-white hover:bg-indigo-600"
                      >
                        Edit
                      </button>
                      <button
                        onClick={() => openDeleteModal(book)}
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
          {Math.max(1, Math.ceil(books.length / PAGE_SIZE))}
        </span>
        <button
          onClick={() =>
            setCurrentPage((p) =>
              Math.min(Math.max(1, Math.ceil(books.length / PAGE_SIZE)), p + 1)
            )
          }
          disabled={
            currentPage >= Math.max(1, Math.ceil(books.length / PAGE_SIZE))
          }
          className={`px-3 py-1 rounded border ${
            currentPage >= Math.max(1, Math.ceil(books.length / PAGE_SIZE))
              ? "text-gray-400 border-gray-200 cursor-not-allowed"
              : "text-gray-700 hover:bg-gray-50"
          }`}
        >
          Next
        </button>
      </div>
      <NewCreateBookModal
        isOpen={isCreateModalOpen}
        onClose={() => setCreateModalOpen(false)}
        onCreate={handleCreate}
      />
      <UpdateBookModal
        isOpen={isUpdateModalOpen}
        onClose={() => {
          setUpdateModalOpen(false);
          setSelectedBook(null);
        }}
        onUpdate={handleUpdate}
        book={selectedBook}
      />
      <DeleteConfirmationModal
        isOpen={isDeleteModalOpen}
        onClose={() => {
          setDeleteModalOpen(false);
          setSelectedBook(null);
        }}
        onConfirm={() => {
          if (selectedBook) {
            handleDelete(selectedBook.id);
          }
        }}
        itemName={selectedBook ? selectedBook.title : ""}
      />
    </div>
  );
};

export default BookManagementTable;
