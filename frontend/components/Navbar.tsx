"use client";

import Link from "next/link";
import { useAuth } from "@/context/AuthContext";
import { useEffect, useState } from "react";
import { useRouter, usePathname } from "next/navigation";

const Navbar = () => {
  const { user, logout } = useAuth();
  const isAuthenticated = !!user;
  const [isDropdownOpen, setDropdownOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState("");
  const router = useRouter();
  const pathname = usePathname();

  useEffect(() => {
    // Clear search box when navigating away from search page
    if (pathname !== "/search") {
      setSearchQuery("");
    }
  }, [pathname]);

  const handleDropdownClick = () => {
    setDropdownOpen(false);
  };

  const handleLogout = () => {
    logout();
    handleDropdownClick();
  };

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      router.push(`/search?query=${searchQuery}`);
      // Optionally clear after navigating to search
      // Comment out if you prefer keeping the query visible on the search page
      setSearchQuery("");
    }
  };

  return (
    <nav className="bg-white shadow-md">
      <div className="container mx-auto px-6 py-3 md:flex md:justify-between md:items-center">
        <div className="flex justify-between items-center">
          <Link href="/" className="flex items-center gap-3">
            <img src="/logo.png" alt="Modern Library" className="h-10 w-auto" />
            <span className="text-xl font-bold text-gray-800 md:text-2xl hover:text-blue-400">
              Modern Library
            </span>
          </Link>

          <div className="hidden md:flex items-center">
            <Link
              href="/catalog"
              className="text-gray-800 hover:text-blue-400 ml-4"
            >
              Catalog
            </Link>
          </div>
        </div>

        <form onSubmit={handleSearchSubmit} className="relative mt-3 md:mt-0">
          <span className="absolute inset-y-0 left-0 pl-3 flex items-center">
            <svg
              className="h-5 w-5 text-gray-500"
              viewBox="0 0 24 24"
              fill="none"
            >
              <path
                d="M21 21L15 15M17 10C17 13.866 13.866 17 10 17C6.13401 17 3 13.866 3 10C3 6.13401 6.13401 3 10 3C13.866 3 17 6.13401 17 10Z"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
              />
            </svg>
          </span>
          <input
            type="text"
            className="w-full bg-gray-200 text-sm rounded-md pl-10 pr-4 py-2 focus:outline-none focus:bg-white focus:text-gray-900"
            placeholder="Search"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === "Escape") setSearchQuery("");
            }}
          />
        </form>

        <div className="mt-3 md:mt-0">
          {isAuthenticated ? (
            <div className="relative">
              <button
                onClick={() => setDropdownOpen(!isDropdownOpen)}
                className="flex items-center text-gray-800"
              >
                <span className="mr-2">{user?.fullName}</span>
                <svg
                  className="h-5 w-5"
                  viewBox="0 0 20 20"
                  fill="currentColor"
                >
                  <path
                    fillRule="evenodd"
                    d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z"
                    clipRule="evenodd"
                  />
                </svg>
              </button>
              {isDropdownOpen && (
                <div className="absolute right-0 mt-2 w-48 bg-white rounded-md shadow-lg py-1">
                  <Link
                    href={(() => {
                      if (user?.roles?.includes("ADMIN"))
                        return "/dashboard/admin";
                      if (user?.roles?.includes("LIBRARIAN"))
                        return "/dashboard/librarian";
                      if (user?.roles?.includes("MEMBER"))
                        return "/dashboard/member";
                      return "/";
                    })()}
                    className="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                    onClick={handleDropdownClick}
                  >
                    Dashboard
                  </Link>
                  <Link
                    href="/profile"
                    className="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                    onClick={handleDropdownClick}
                  >
                    Profile
                  </Link>
                  <button
                    onClick={handleLogout}
                    className="block w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                  >
                    Logout
                  </button>
                </div>
              )}
            </div>
          ) : (
            <Link
              href="/auth/login"
              className="px-4 py-2 bg-blue-500 text-white text-sm font-medium rounded hover:bg-blue-400 focus:outline-none focus:bg-blue-400"
            >
              Login / Sign Up
            </Link>
          )}
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
