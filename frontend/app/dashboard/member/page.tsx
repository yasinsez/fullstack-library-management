"use client";

import { useAuth } from "../../../context/AuthContext";
import BorrowedBooks from "../../../components/dashboard/BorrowedBooks";
import ReadingHistory from "../../../components/dashboard/ReadingHistory";

const MemberDashboardPage = () => {
  const { user } = useAuth();

  if (!user) {
    return (
      <div className="text-center py-10">
        <p>You must be logged in to view this page.</p>
      </div>
    );
  }

  return (
    <div className="container mx-auto py-10">
      <h1 className="text-3xl font-bold mb-6">Member Dashboard</h1>
      <p className="mb-8">Welcome, {user.fullName}!</p>

      <BorrowedBooks />
      <ReadingHistory />
    </div>
  );
};

export default MemberDashboardPage;
