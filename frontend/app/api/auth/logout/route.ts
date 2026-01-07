import { serialize } from "cookie";
import { NextResponse } from "next/server";

export async function POST() {
  const serialized = serialize("token", "", {
    httpOnly: true,
    secure: false,
    sameSite: "lax",
    maxAge: -1,
    path: "/",
  });

  return new NextResponse(JSON.stringify({ status: "success" }), {
    status: 200,
    headers: { "Set-Cookie": serialized },
  });
}
