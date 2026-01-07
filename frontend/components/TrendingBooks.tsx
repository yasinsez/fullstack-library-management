"use client";
import React, { useState, useEffect } from "react";
import { publicApiClient } from "../lib/apiClient";
import BookCard from "./BookCard";

type TrendingBook = {
  id: number;
  title: string;
  authorName: string;
  coverImageUrl: string;
  isAvailable: boolean;
};

const TrendingBooks = () => {
  const [books, setBooks] = useState<TrendingBook[]>([]);

  useEffect(() => {
    const fetchTrendingBooks = async () => {
      try {
        const response = await publicApiClient.get("/books/trending");
        setBooks(response.data);
      } catch (err) {
        console.error("Failed to fetch trending books:", err);
      }
    };
    fetchTrendingBooks();
  }, []);

  return (
    <div className="py-8">
      <h2 className="text-2xl font-bold mb-6">Trending Books</h2>
      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-6 gap-6">
        {books.map((book) => (
          <BookCard
            key={book.id}
            id={book.id}
            title={book.title}
            author={book.authorName}
            imageUrl={book.coverImageUrl || "/images/default_book_image.jpeg"}
            isAvailable={book.isAvailable}
          />
        ))}
      </div>
    </div>
  );
};

export default TrendingBooks;
