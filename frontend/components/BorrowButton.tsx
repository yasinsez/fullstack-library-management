"use client";

import { useState, useEffect } from "react";
import { useAuth } from "@/context/AuthContext";
// Use Next API proxy to avoid reading httpOnly token on client
import toast from "react-hot-toast";

type BorrowButtonProps = {
  bookId: number;
  isAvailable: boolean;
  onBorrowSuccess?: () => void;
  onReserveSuccess?: (position?: number) => void;
  className?: string;
};

const BorrowButton = ({
  bookId,
  isAvailable,
  onBorrowSuccess,
  onReserveSuccess,
  className,
}: BorrowButtonProps) => {
  const { user } = useAuth();
  const [available, setAvailable] = useState<boolean>(isAvailable);
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [alreadyBorrowed, setAlreadyBorrowed] = useState<boolean>(false);
  const [alreadyReserved, setAlreadyReserved] = useState<boolean>(false);

  useEffect(() => {
    setAvailable(isAvailable);
  }, [isAvailable]);

  const isMember =
    !!user && Array.isArray(user.roles) && user.roles.includes("MEMBER");

  useEffect(() => {
    const checkAlreadyBorrowed = async () => {
      if (!user || !isMember) {
        setAlreadyBorrowed(false);
        setAlreadyReserved(false);
        return;
      }
      try {
        const resp = await fetch(`/api/loans`, {
          method: "GET",
          cache: "no-store",
        });
        if (!resp.ok) {
          return; // silently ignore
        }
        const loans = await resp.json();
        const hasLoan =
          Array.isArray(loans) &&
          loans.some(
            (loan: { book?: { id?: number } }) => loan?.book?.id === bookId
          );
        setAlreadyBorrowed(hasLoan);
      } catch (_) {
        // ignore
      }
    };
    checkAlreadyBorrowed();
  }, [user, isMember, bookId]);

  useEffect(() => {
    const checkAlreadyReserved = async () => {
      if (!user || !isMember) {
        setAlreadyReserved(false);
        return;
      }
      try {
        const resp = await fetch(`/api/reservations`, {
          method: "GET",
          cache: "no-store",
        });
        if (!resp.ok) return;
        const reservations = await resp.json();
        const hasActiveReservation =
          Array.isArray(reservations) &&
          reservations.some(
            (r: { book?: { id?: number }; status?: string }) =>
              r?.book?.id === bookId &&
              (r?.status === "PENDING" || r?.status === "READY_FOR_PICKUP")
          );
        setAlreadyReserved(hasActiveReservation);
      } catch (_) {
        // ignore
      }
    };
    checkAlreadyReserved();
  }, [user, isMember, bookId]);

  const handleBorrow = async () => {
    if (!user) {
      toast.error("You must be logged in to borrow a book.");
      return;
    }

    if (!isMember) {
      toast.error("You do not have permission to borrow books.");
      return;
    }

    if (!available || alreadyBorrowed) {
      // When not available, offer reservation
      if (!available) {
        try {
          setIsSubmitting(true);
          const resp = await fetch(`/api/reservations`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ bookId }),
          });
          if (!resp.ok) {
            const text = await resp.text();
            throw new Error(text || "Reservation failed");
          }
          let position: number | undefined = undefined;
          try {
            const body = await resp.json();
            if (
              body &&
              typeof body === "object" &&
              typeof body.priorityNumber === "number"
            ) {
              position = body.priorityNumber as number;
            }
          } catch (_) {
            // ignore json parse
          }
          toast.success("Reserved. We'll notify you when it's ready!");
          setAlreadyReserved(true);
          onReserveSuccess?.(position);
        } catch (e) {
          const errMsg = (e as Error)?.message || "Failed to reserve book.";
          if (errMsg.includes("active reservation")) {
            setAlreadyReserved(true);
            toast("Already reserved.");
          } else if (errMsg.includes("maximum number of active reservations")) {
            toast.error("You reached the maximum active reservations (3).");
          } else {
            toast.error("Failed to reserve book.");
          }
        } finally {
          setIsSubmitting(false);
        }
      }
      return;
    }

    try {
      setIsSubmitting(true);
      const resp = await fetch(`/api/loans/borrow?bookId=${bookId}`, {
        method: "POST",
      });
      if (!resp.ok) {
        const text = await resp.text();
        throw new Error(text || "Borrow failed");
      }
      toast.success("Book borrowed successfully!");
      setAvailable(false);
      setAlreadyBorrowed(true);
      onBorrowSuccess?.();
    } catch (error: unknown) {
      const err = error as { response?: { data?: unknown } } | undefined;
      const message = err?.response?.data;
      toast.error(
        typeof message === "string" ? message : "Failed to borrow book."
      );
    } finally {
      setIsSubmitting(false);
    }
  };

  const isReserveState =
    !available && isMember && !alreadyBorrowed && !alreadyReserved;
  const disabled =
    !isMember || alreadyBorrowed || isSubmitting || alreadyReserved;
  const label = alreadyReserved
    ? "Already Reserved"
    : alreadyBorrowed
    ? "Already Borrowed"
    : !available
    ? isMember
      ? isSubmitting
        ? "Reserving..."
        : "Reserve"
      : "Unavailable"
    : !user
    ? "Sign in to borrow"
    : !isMember
    ? "Not eligible"
    : isSubmitting
    ? "Borrowing..."
    : "Borrow";

  return (
    <button
      onClick={handleBorrow}
      disabled={disabled}
      className={
        `w-full py-2 rounded-md transition-colors ` +
        (alreadyReserved
          ? "bg-amber-300 text-amber-900 cursor-not-allowed"
          : disabled
          ? "bg-gray-400 text-gray-700 cursor-not-allowed"
          : isReserveState
          ? "bg-amber-500 text-white hover:bg-amber-600"
          : "bg-blue-500 text-white hover:bg-blue-600") +
        (className ? ` ${className}` : "")
      }
    >
      {label}
    </button>
  );
};

export default BorrowButton;
