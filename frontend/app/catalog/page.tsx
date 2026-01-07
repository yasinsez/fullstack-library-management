"use client";
import React, { useState, useEffect } from "react";
import { publicApiClient } from "../../lib/apiClient";
import BookCard from "../../components/BookCard";

type Book = {
  id: number;
  title: string;
  authorName: string;
  coverImageUrl: string;
  isAvailable: boolean;
};

type Category = {
  id: number;
  name: string;
};

const BookListPage = () => {
  const [books, setBooks] = useState<Book[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState("");
  const [debouncedSearchQuery, setDebouncedSearchQuery] = useState("");
  const [selectedGenre, setSelectedGenre] = useState("");
  const [genres, setGenres] = useState<Category[]>([]);

  useEffect(() => {
    const fetchGenres = async () => {
      try {
        const response = await publicApiClient.get("/books/genres");
        setGenres(response.data);
      } catch (err) {
        console.error("Failed to fetch genres:", err);
      }
    };

    fetchGenres();
  }, []);

  useEffect(() => {
    const timer = setTimeout(() => {
      setDebouncedSearchQuery(searchQuery);
    }, 500);

    return () => {
      clearTimeout(timer);
    };
  }, [searchQuery]);

  useEffect(() => {
    const fetchBooks = async () => {
      setLoading(true);
      try {
        let url = "/books";
        if (debouncedSearchQuery) {
          url = `/books/search?query=${debouncedSearchQuery}`;
        } else if (selectedGenre) {
          url = `/books/genre/${selectedGenre}`;
        }
        const response = await publicApiClient.get(url);
        setBooks(response.data);
      } catch (err) {
        setError("Failed to fetch books. Please try again later.");
        console.error("Failed to fetch books:", err);
      } finally {
        setLoading(false);
      }
    };

    fetchBooks();
  }, [debouncedSearchQuery, selectedGenre]);

  const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setSearchQuery(e.target.value);
    setSelectedGenre("");
  };

  const handleGenreChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setSelectedGenre(e.target.value);
    setSearchQuery("");
  };

  if (error) {
    return <div className="text-center py-10 text-red-500">{error}</div>;
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold mb-8">Book Catalog</h1>
      <div className="flex justify-between items-center mb-8">
        <div className="w-1/2">
          <input
            type="text"
            placeholder="Search by title or author..."
            value={searchQuery}
            onChange={handleSearchChange}
            className="w-full px-4 py-2 border rounded-md bg-white text-black"
          />
        </div>
        <div className="w-1/4">
          <select
            value={selectedGenre}
            onChange={handleGenreChange}
            className="w-full px-4 py-2 border rounded-md bg-white text-black"
          >
            <option value="">All Genres</option>
            {genres.map((genre) => (
              <option key={genre.id} value={genre.id}>
                {genre.name}
              </option>
            ))}
          </select>
        </div>
      </div>
      {loading ? (
        <div className="text-center py-10">Loading...</div>
      ) : (
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
      )}
    </div>
  );
};

export default BookListPage;
