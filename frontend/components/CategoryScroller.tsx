"use client";
import React, { useState, useEffect } from "react";
import { publicApiClient } from "../lib/apiClient";
import { useRouter } from "next/navigation";

type Category = { id: number; name: string };

const CategoryScroller = () => {
  const [categories, setCategories] = useState<Category[]>([]);
  const router = useRouter();

  useEffect(() => {
    const fetchCategories = async () => {
      try {
        const response = await publicApiClient.get("/books/genres");
        setCategories(response.data);
      } catch (err) {
        console.error("Failed to fetch categories:", err);
      }
    };

    fetchCategories();
  }, []);

  const handleCategoryClick = (category: Category) => {
    router.push(
      `/search?categoryId=${category.id}&categoryName=${encodeURIComponent(
        category.name
      )}`
    );
  };

  return (
    <div className="py-4">
      <h2 className="text-2xl font-bold mb-4">Browse by Category</h2>
      <div className="flex space-x-4 overflow-x-auto pb-4">
        {categories.map((category) => (
          <div key={category.id} className="flex-shrink-0">
            <button
              type="button"
              onClick={() => handleCategoryClick(category)}
              className="block bg-gray-700 text-white hover:bg-gray-600 rounded-full px-4 py-2 font-semibold shadow-md transition-colors duration-300"
            >
              {category.name}
            </button>
          </div>
        ))}
      </div>
    </div>
  );
};

export default CategoryScroller;
