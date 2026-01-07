"use client";

import { useState, useEffect } from "react";
import { Author } from "./AuthorManagementTable";

interface UpdateAuthorModalProps {
  isOpen: boolean;
  onClose: () => void;
  onUpdate: (id: number, authorData: Partial<Author>) => void;
  author: Author | null;
}

const UpdateAuthorModal = ({
  isOpen,
  onClose,
  onUpdate,
  author,
}: UpdateAuthorModalProps) => {
  const [name, setName] = useState("");
  const [biography, setBiography] = useState("");
  const [dateOfBirth, setDateOfBirth] = useState("");

  useEffect(() => {
    if (author) {
      setName(author.name);
      setBiography(author.biography);
      setDateOfBirth(author.dateOfBirth);
    }
  }, [author]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (author) {
      onUpdate(author.id, { name, biography, dateOfBirth });
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full text-black">
      <div className="relative top-20 mx-auto p-5 border w-full max-w-2xl shadow-lg rounded-md bg-white">
        <div className="mt-3 text-center">
          <h3 className="text-lg leading-6 font-medium text-gray-900">
            Edit Author
          </h3>
          <form className="mt-4" onSubmit={handleSubmit}>
            <div className="grid grid-cols-1 gap-4">
              <div className="mb-4">
                <label
                  htmlFor="name"
                  className="block text-sm font-medium text-gray-700 text-left"
                >
                  Name
                </label>
                <input
                  type="text"
                  name="name"
                  id="name"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                  required
                />
              </div>
              <div className="mb-4">
                <label
                  htmlFor="biography"
                  className="block text-sm font-medium text-gray-700 text-left"
                >
                  Biography
                </label>
                <textarea
                  name="biography"
                  id="biography"
                  value={biography}
                  onChange={(e) => setBiography(e.target.value)}
                  className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                />
              </div>
              <div className="mb-4">
                <label
                  htmlFor="dateOfBirth"
                  className="block text-sm font-medium text-gray-700 text-left"
                >
                  Date of Birth
                </label>
                <input
                  type="date"
                  name="dateOfBirth"
                  id="dateOfBirth"
                  value={dateOfBirth}
                  onChange={(e) => setDateOfBirth(e.target.value)}
                  className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                />
              </div>
            </div>
            <div className="items-center px-4 py-3 sm:flex sm:flex-row-reverse">
              <button
                type="submit"
                className="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-indigo-600 text-base font-medium text-white hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 sm:ml-3 sm:w-auto sm:text-sm"
              >
                Update Author
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

export default UpdateAuthorModal;
