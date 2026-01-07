import { NextRequest, NextResponse } from "next/server";

export async function POST(req: NextRequest) {
  const body = await req.json();
  const { username, password } = body;

  try {
    const backendResponse = await fetch(
      "http://localhost:8083/api/auth/login",
      {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, password }),
      }
    );

    if (!backendResponse.ok) {
      const errorData = await backendResponse.json();
      return NextResponse.json(
        { message: errorData.message || "Invalid credentials" },
        { status: backendResponse.status }
      );
    }

    const { token } = await backendResponse.json();

    const response = NextResponse.json({ ok: true });
    response.cookies.set({
      name: "token",
      value: token,
      httpOnly: true,
      secure: false,
      path: "/",
      sameSite: "lax",
      maxAge: 60 * 60, // 1 hour
    });

    return response;
  } catch (error) {
    return NextResponse.json(
      { message: "An internal server error occurred" },
      { status: 500 }
    );
  }
}
