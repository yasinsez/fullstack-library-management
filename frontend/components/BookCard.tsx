import Image from "next/image";
import Link from "next/link";
import { useState, useEffect } from "react";
import BorrowButton from "./BorrowButton";

type BookCardProps = {
  id: number;
  title: string;
  author: string;
  imageUrl: string;
  isAvailable: boolean;
};

const BookCard = ({
  id,
  title,
  author,
  imageUrl,
  isAvailable,
}: BookCardProps) => {
  const [available, setAvailable] = useState(isAvailable);
  const placeholderImage = "/images/default_book_image.jpeg";

  useEffect(() => {
    setAvailable(isAvailable);
  }, [isAvailable]);

  let imageSrc = placeholderImage;
  if (imageUrl && imageUrl.trim() !== "") {
    if (imageUrl.startsWith("http")) {
      imageSrc = imageUrl;
    } else {
      imageSrc = `http://localhost:8083${imageUrl}`;
    }
  }

  const handleBorrowSuccess = () => setAvailable(false);

  return (
    <div className="border rounded-lg overflow-hidden shadow-lg hover:shadow-xl transition-shadow duration-300 h-full flex flex-col">
      <Link href={`/books/${id}`} className="flex-grow">
        <div className="relative w-full h-64">
          <Image
            src={imageSrc}
            alt={`Cover of ${title}`}
            layout="fill"
            objectFit="cover"
          />
        </div>
        <div className="p-4">
          <h3 className="text-lg font-bold">{title}</h3>
          <p className="text-gray-400">{author}</p>
        </div>
      </Link>
      <div className="p-4 border-t">
        <BorrowButton
          bookId={id}
          isAvailable={available}
          onBorrowSuccess={handleBorrowSuccess}
        />
      </div>
    </div>
  );
};

export default BookCard;
