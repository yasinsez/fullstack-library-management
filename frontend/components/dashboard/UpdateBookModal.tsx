"use client";

import { useState, useEffect } from "react";
import { Book, Author } from "./BookManagementTable";
import { apiClient } from "../../lib/apiClient";

interface Publisher {
  id: number;
  name: string;
}

interface Category {
  id: number;
  name: string;
}

interface UpdateBookModalProps {
  isOpen: boolean;
  onClose: () => void;
  onUpdate: (
    id: number,
    bookData: Partial<
      Omit<Book, "id" | "quantity" | "authors"> & {
        authorIds: number[];
        publisherId: number;
        categoryId: number;
        stock: number;
      }
    >
  ) => void;
  book: Book | null;
}

const UpdateBookModal = ({
  isOpen,
  onClose,
  onUpdate,
  book,
}: UpdateBookModalProps) => {
  const [title, setTitle] = useState("");
  const [isbn, setIsbn] = useState("");
  const [publicationYear, setPublicationYear] = useState<number | undefined>();
  const [description, setDescription] = useState<string | undefined>();
  const [coverImage, setCoverImage] = useState<string | undefined>();
  const [pages, setPages] = useState<number | undefined>();
  const [stock, setStock] = useState(0);
  const [selectedAuthors, setSelectedAuthors] = useState<number[]>([]);
  const [publisherId, setPublisherId] = useState<number | "">("");
  const [categoryId, setCategoryId] = useState<number | "">("");
  const [allAuthors, setAllAuthors] = useState<Author[]>([]);
  const [allPublishers, setAllPublishers] = useState<Publisher[]>([]);
  const [allCategories, setAllCategories] = useState<Category[]>([]);

  useEffect(() => {
    const fetchDropdownData = async () => {
      try {
        const [authorsRes, publishersRes, categoriesRes] = await Promise.all([
          apiClient.get("/authors"),
          apiClient.get("/publishers"),
          apiClient.get("/categories"),
        ]);
        setAllAuthors(authorsRes.data);
        setAllPublishers(publishersRes.data);
        setAllCategories(categoriesRes.data);
      } catch (error) {
        console.error("Failed to fetch dropdown data:", error);
      }
    };

    if (isOpen) {
      fetchDropdownData();
    }
  }, [isOpen]);

  useEffect(() => {
    if (!book) return;

    setTitle(book.title);
    setIsbn(book.isbn);
    setPublicationYear(book.publicationYear);
    setDescription(book.description);
    setCoverImage(book.coverImage);
    setPages(book.pages);
    setStock(book.quantity);
    setSelectedAuthors(book.authors.map((author) => author.id));

    // Resolve publisherId: backend list endpoint returns publisher name only, so local row may have id=0
    if (
      book.publisher &&
      typeof book.publisher.id === "number" &&
      book.publisher.id > 0
    ) {
      setPublisherId(book.publisher.id);
    } else if (
      book.publisher &&
      book.publisher.name &&
      allPublishers.length > 0
    ) {
      const match = allPublishers.find((p) => p.name === book.publisher.name);
      setPublisherId(match ? match.id : "");
    } else {
      setPublisherId("");
    }

    // Resolve categoryId similarly (category may not be present in list response)
    if (
      book.category &&
      typeof book.category.id === "number" &&
      book.category.id > 0
    ) {
      setCategoryId(book.category.id);
    } else if (
      book.category &&
      book.category.name &&
      allCategories.length > 0
    ) {
      const matchCat = allCategories.find((c) => c.name === book.category.name);
      setCategoryId(matchCat ? matchCat.id : "");
    } else {
      setCategoryId("");
    }
  }, [book, allPublishers, allCategories]);

  const handleAuthorChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const selectedOptions = Array.from(e.target.selectedOptions, (option) =>
      Number(option.value)
    );
    setSelectedAuthors(selectedOptions);
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (
      book &&
      publisherId !== "" &&
      categoryId !== "" &&
      selectedAuthors.length > 0
    ) {
      onUpdate(book.id, {
        title,
        isbn,
        publicationYear,
        description,
        coverImage,
        pages,
        authorIds: selectedAuthors,
        publisherId: Number(publisherId),
        categoryId: Number(categoryId),
        stock: Number(stock),
      });
    } else {
      alert("Please fill all fields and select at least one author.");
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full text-black">
      <div className="relative top-20 mx-auto p-5 border w-full max-w-2xl shadow-lg rounded-md bg-white">
        <div className="mt-3 text-center">
          <h3 className="text-lg leading-6 font-medium text-gray-900">
            Edit Book
          </h3>
          <form className="mt-4" onSubmit={handleSubmit}>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="mb-4">
                <label
                  htmlFor="title"
                  className="block text-sm font-medium text-gray-700 text-left"
                >
                  Title
                </label>
                <input
                  type="text"
                  name="title"
                  id="title"
                  value={title}
                  onChange={(e) => setTitle(e.target.value)}
                  className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                />
              </div>
              <div className="mb-4">
                <label
                  htmlFor="isbn"
                  className="block text-sm font-medium text-gray-700 text-left"
                >
                  ISBN
                </label>
                <input
                  type="text"
                  name="isbn"
                  id="isbn"
                  value={isbn}
                  onChange={(e) => setIsbn(e.target.value)}
                  className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                />
              </div>
              <div className="mb-4">
                <label
                  htmlFor="stock"
                  className="block text-sm font-medium text-gray-700 text-left"
                >
                  Stock
                </label>
                <input
                  type="number"
                  name="stock"
                  id="stock"
                  value={stock}
                  onChange={(e) => setStock(Number(e.target.value))}
                  className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                />
              </div>
              <div className="mb-4">
                <label
                  htmlFor="authors"
                  className="block text-sm font-medium text-gray-700 text-left"
                >
                  Authors
                </label>
                <select
                  multiple
                  name="authors"
                  id="authors"
                  value={selectedAuthors.map(String)}
                  onChange={handleAuthorChange}
                  className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                >
                  {allAuthors.map((author) => (
                    <option key={author.id} value={author.id}>
                      {author.name}
                    </option>
                  ))}
                </select>
              </div>
              <div className="mb-4">
                <label
                  htmlFor="publisherId"
                  className="block text-sm font-medium text-gray-700 text-left"
                >
                  Publisher
                </label>
                <select
                  name="publisherId"
                  id="publisherId"
                  value={publisherId}
                  onChange={(e) => setPublisherId(Number(e.target.value))}
                  className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                >
                  <option value="" disabled>
                    Select a publisher
                  </option>
                  {allPublishers.map((publisher) => (
                    <option key={publisher.id} value={publisher.id}>
                      {publisher.name}
                    </option>
                  ))}
                </select>
              </div>
              <div className="mb-4">
                <label
                  htmlFor="categoryId"
                  className="block text-sm font-medium text-gray-700 text-left"
                >
                  Category
                </label>
                <select
                  name="categoryId"
                  id="categoryId"
                  value={categoryId}
                  onChange={(e) => setCategoryId(Number(e.target.value))}
                  className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                >
                  <option value="" disabled>
                    Select a category
                  </option>
                  {allCategories.map((category) => (
                    <option key={category.id} value={category.id}>
                      {category.name}
                    </option>
                  ))}
                </select>
              </div>
            </div>
            <div className="items-center px-4 py-3 sm:flex sm:flex-row-reverse">
              <button
                type="submit"
                className="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-indigo-600 text-base font-medium text-white hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 sm:ml-3 sm:w-auto sm:text-sm"
              >
                Update Book
              </button>
              <button
                type="button"
                onClick={onClose}
                className="mt-3 w-full inline-flex justify-center rounded-md border border-gray-300 shadow-sm px-4 py-2 bg-white text-base font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 sm:mt-0 sm:w-auto sm:text-sm"
              >
                Cancel
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default UpdateBookModal;
