import axios from "axios";
import Cookies from "js-cookie";

const createApiClient = (withAuth: boolean = true) => {
  const instance = axios.create({
    baseURL: process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8083/api",
    headers: {
      "Content-Type": "application/json",
    },
    withCredentials: true,
  });

  if (withAuth) {
    instance.interceptors.request.use(
      (config) => {
        if (typeof window !== "undefined") {
          const token = Cookies.get("token");
          if (token) {
            config.headers.Authorization = `Bearer ${token}`;
          }
        }
        return config;
      },
      (error) => {
        return Promise.reject(error);
      }
    );
  }

  instance.interceptors.response.use(
    (response) => response,
    (error) => {
      return Promise.reject(error);
    }
  );

  return instance;
};

export const apiClient = createApiClient(true);
export const publicApiClient = createApiClient(false);

