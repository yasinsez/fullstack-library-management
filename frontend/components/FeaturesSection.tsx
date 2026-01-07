import Link from "next/link";

export default function FeaturesSection() {
  return (
    <section id="features" className="bg-white py-16 sm:py-20">
      <div className="mx-auto max-w-6xl px-6">
        <h2 className="text-center text-3xl font-bold tracking-tight text-gray-900 sm:text-4xl">
          Our Library Features
        </h2>
        <div className="mt-12 grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
          <FeatureCard
            icon={<i className="fas fa-book" aria-hidden="true" />}
            title="Extensive Collection"
            desc="Access over 50,000 books, journals, and digital media across all subjects and interests."
            linkLabel="Browse Catalog"
            href="/catalog"
          />
          <FeatureCard
            icon={<i className="fas fa-laptop" aria-hidden="true" />}
            title="Digital Resources"
            desc="Enjoy 24/7 access to e-books, research databases, and online learning materials."
            linkLabel="Explore Digital Library"
            href="/catalog"
          />
          <FeatureCard
            icon={<i className="fas fa-users" aria-hidden="true" />}
            title="Community Programs"
            desc="Participate in book clubs, workshops, and educational events for all ages."
            linkLabel="View Calendar"
            href="#"
          />
        </div>
      </div>
    </section>
  );
}

function FeatureCard({
  icon,
  title,
  desc,
  href,
  linkLabel,
}: {
  icon: React.ReactNode;
  title: string;
  desc: string;
  href: string;
  linkLabel: string;
}) {
  return (
    <div className="rounded-lg border border-gray-200 bg-white shadow-sm transition hover:-translate-y-1 hover:shadow-md">
      <div className="flex h-36 items-center justify-center bg-blue-600 text-3xl text-white">
        {icon}
      </div>
      <div className="p-6">
        <h3 className="text-lg font-semibold text-gray-900">{title}</h3>
        <p className="mt-2 text-gray-600">{desc}</p>
        <Link
          href={href}
          className="mt-4 inline-block font-semibold text-blue-600 underline-offset-4 hover:underline"
        >
          {linkLabel}
        </Link>
      </div>
    </div>
  );
}
