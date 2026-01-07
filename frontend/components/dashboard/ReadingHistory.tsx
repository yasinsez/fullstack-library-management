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
  returnDate: string;
}

const ReadingHistory = () => {
  const [history, setHistory] = useState<Loan[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchReadingHistory = async () => {
      try {
        setLoading(true);
        const response = await fetch("/api/loans/history", { cache: "no-store" });
        if (!response.ok) {
          throw new Error(await response.text());
        }
        const data = await response.json();
        setHistory(data);
        setError(null);
      } catch (error) {
        console.error("Failed to fetch reading history:", error);
        setError("Failed to fetch reading history. Please try again later.");
        toast.error("Failed to fetch reading history.");
      } finally {
        setLoading(false);
      }
    };
    fetchReadingHistory();
  }, []);

  if (loading) {
    return <p>Loading reading history...</p>;
  }

  if (error) {
    return <p className="text-red-500">{error}</p>;
  }

  return (
    <div className="mt-8">
      <h2 className="text-2xl font-bold mb-4">Reading History</h2>
      {history.length === 0 ? (
        <p>You have no reading history.</p>
      ) : (
        <ul className="space-y-4">
          {history.map((loan) => (
            <li
              key={loan.id}
              className="p-4 border rounded-lg shadow-sm flex justify-between items-center"
            >
              <div>
                <h3 className="font-bold">{loan.book.title}</h3>
                <p className="text-sm text-gray-600">{loan.book.authorName}</p>
              </div>
              <p className="text-sm">
                <strong>Returned on:</strong>{" "}
                {new Date(loan.returnDate).toLocaleDateString()}
              </p>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
};

export default ReadingHistory;
