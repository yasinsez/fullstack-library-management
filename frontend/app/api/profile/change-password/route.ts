import { NextRequest, NextResponse } from "next/server";

const BACKEND_URL = "http://localhost:8083/api/profile/change-password";

export async function POST(request: NextRequest) {
  const token = request.cookies.get("token")?.value;
  if (!token) {
    return NextResponse.json({ message: "Unauthorized" }, { status: 401 });
  }

  try {
    const body = await request.json();
    const resp = await fetch(BACKEND_URL, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify(body),
      cache: "no-store",
    });
    if (!resp.ok) {
      const text = await resp.text();
      return NextResponse.json({ message: text || "Failed" }, { status: resp.status });
    }
    return new NextResponse(null, { status: 204 });
  } catch (err) {
    console.error("POST /api/profile/change-password failed", err);
    return NextResponse.json({ message: "Internal Server Error" }, { status: 500 });
  }
}


