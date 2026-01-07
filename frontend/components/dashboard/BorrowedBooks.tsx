"use client";
import React, { useState, useEffect } from "react";
import { apiClient } from "../../lib/apiClient";
import { toast } from "react-hot-toast";

interface Book {
  title: string;
  authorName: string;
}

interface Loan {
  id: number;
  book: Book;
  dueDate: string;
}

const BorrowedBooks = () => {
  const [books, setBooks] = useState<Loan[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchBorrowedBooks = async () => {
    try {
      setLoading(true);
      // Prefer routing via Next API to attach httpOnly cookie properly
      const response = await fetch("/api/loans", { cache: "no-store" });
      if (!response.ok) {
        throw new Error(await response.text());
      }
      const data = await response.json();
      setBooks(data);
      setError(null);
    } catch (error) {
      console.error("Failed to fetch borrowed books:", error);
      setError("Failed to fetch borrowed books. Please try again later.");
      toast.error("Failed to fetch borrowed books.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchBorrowedBooks();
  }, []);

  const handleReturn = async (loanId: number) => {
    try {
      // Use Next API proxy for return so httpOnly token is used
      const resp = await fetch(`/api/loans/return?loanId=${loanId}`, {
        method: "PUT",
      });
      if (!resp.ok) {
        throw new Error(await resp.text());
      }
      toast.success("Book returned successfully!");
      fetchBorrowedBooks();
    } catch (error) {
      console.error("Failed to return book:", error);
      toast.error("Failed to return book.");
    }
  };

  if (loading) {
    return <p>Loading borrowed books...</p>;
  }

  if (error) {
    return <p className="text-red-500">{error}</p>;
  }

  return (
    <div className="mt-8">
      <h2 className="text-2xl font-bold mb-4">Borrowed Books</h2>
      {books.length === 0 ? (
        <p>You have no borrowed books.</p>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {books.map((loan) => (
            <div key={loan.id} className="p-4 border rounded-lg shadow-sm">
              <h3 className="font-bold">{loan.book.title}</h3>
              <p className="text-sm text-gray-600">{loan.book.authorName}</p>
              <p className="text-sm mt-2">
                <strong>Due Date:</strong>{" "}
                {new Date(loan.dueDate).toLocaleDateString()}
              </p>
              <button
                onClick={() => handleReturn(loan.id)}
                className="mt-4 w-full bg-blue-500 text-white py-2 rounded hover:bg-blue-600 transition-colors duration-300"
              >
                Return
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default BorrowedBooks;
