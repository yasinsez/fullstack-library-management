const Footer = () => {
  return (
    <footer className="bg-gray-800 text-white py-8">
      <div className="container mx-auto px-6">
        <div className="flex flex-wrap">
          <div className="w-full md:w-1/4 text-center md:text-left">
            <h5 className="uppercase mb-6 font-bold">Links</h5>
            <ul className="mb-4">
              <li className="mt-2">
                <a href="#" className="hover:underline">
                  FAQ
                </a>
              </li>
              <li className="mt-2">
                <a href="#" className="hover:underline">
                  Help
                </a>
              </li>
              <li className="mt-2">
                <a href="#" className="hover:underline">
                  Support
                </a>
              </li>
            </ul>
          </div>
          <div className="w-full md:w-1/4 text-center md:text-left">
            <h5 className="uppercase mb-6 font-bold">Legal</h5>
            <ul className="mb-4">
              <li className="mt-2">
                <a href="#" className="hover:underline">
                  Terms
                </a>
              </li>
              <li className="mt-2">
                <a href="#" className="hover:underline">
                  Privacy
                </a>
              </li>
            </ul>
          </div>
          <div className="w-full md:w-1/4 text-center md:text-left">
            <h5 className="uppercase mb-6 font-bold">Social</h5>
            <ul className="mb-4">
              <li className="mt-2">
                <a href="#" className="hover:underline">
                  Facebook
                </a>
              </li>
              <li className="mt-2">
                <a href="#" className="hover:underline">
                  Linkedin
                </a>
              </li>
              <li className="mt-2">
                <a href="#" className="hover:underline">
                  Twitter
                </a>
              </li>
            </ul>
          </div>
          <div className="w-full md:w-1/4 text-center md:text-left">
            <h5 className="uppercase mb-6 font-bold">Company</h5>
            <ul className="mb-4">
              <li className="mt-2">
                <a href="#" className="hover:underline">
                  About Us
                </a>
              </li>
              <li className="mt-2">
                <a href="#" className="hover:underline">
                  Contact
                </a>
              </li>
            </ul>
          </div>
        </div>
        <div className="text-center pt-8 mt-8 border-t border-gray-700">
          <p>&copy; 2024 Modern Library. All rights reserved.</p>
        </div>
      </div>
    </footer>
  );
};

export default Footer;

