"use client";

import { useState } from "react";
import Image from "next/image";

export default function HeroSection() {
  const [isExpanded, setIsExpanded] = useState(false);

  return (
    <section className="relative isolate overflow-hidden bg-[url('/background.webp')] bg-cover bg-center">
      <div className="absolute inset-0 bg-black/60" />

      <div className="relative mx-auto max-w-5xl px-6 py-24 text-center text-white md:py-32">
        <div className="mx-auto max-w-3xl">
          <div className="mb-6 flex items-center justify-center gap-3">
            <Image
              src="/logo.png"
              alt="Modern Library"
              width={64}
              height={64}
              priority
            />
            <h1 className="text-3xl font-extrabold tracking-tight sm:text-4xl">
              Modern Library System
            </h1>
          </div>
          <p className="mx-auto max-w-2xl text-base/7 opacity-90 sm:text-lg">
            {isExpanded
              ? "Discover a world of knowledge at your fingertips. Our modern library system offers extensive collections, digital resources, and innovative services to support your learning journey."
              : "Discover a world of knowledge at your fingertips..."}
          </p>

          <div className="mt-8 flex items-center justify-center gap-4">
            <button
              type="button"
              onClick={() => setIsExpanded(!isExpanded)}
              className="rounded-full bg-blue-600 px-6 py-3 text-sm font-semibold text-white shadow-md transition hover:bg-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-400"
            >
              {isExpanded ? "Show Less" : "Read More"}
            </button>
            <a
              href="#features"
              className="rounded-full border border-white/70 px-6 py-3 text-sm font-semibold text-white transition hover:bg-white/10 focus:outline-none focus:ring-2 focus:ring-white/40"
            >
              Explore Features
            </a>
          </div>
        </div>
      </div>
    </section>
  );
}
