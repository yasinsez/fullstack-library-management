import CategoryScroller from "@/components/CategoryScroller";
import TrendingBooks from "@/components/TrendingBooks";
import HeroSection from "@/components/HeroSection";
import FeaturesSection from "@/components/FeaturesSection";

export default function Home() {
  return (
    <div>
      <HeroSection />
      <FeaturesSection />
      <CategoryScroller />
      <TrendingBooks />
    </div>
  );
}
