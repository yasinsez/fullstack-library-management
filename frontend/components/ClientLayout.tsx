"use client";

import React from "react";
import { useAuth } from "@/context/AuthContext";
import Navbar from "@/components/Navbar";
import Footer from "@/components/Footer";
import { Toaster } from "react-hot-toast";

const ClientLayout = ({ children }: { children: React.ReactNode }) => {
  const { isAuthReady } = useAuth();

  return (
    <>
      <Toaster position="top-center" />
      {isAuthReady && <Navbar />}
      <main className="flex-grow container mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {children}
      </main>
      {isAuthReady && <Footer />}
    </>
  );
};

export default ClientLayout;
