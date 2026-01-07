import { NextResponse } from "next/server";
import { cookies } from "next/headers";

const BACKEND_BASE = process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8083/api";

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export async function PUT(_request: Request, { params }: any) {
  const token = (await cookies()).get("token")?.value;
  if (!token) return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
  const { id } = await params;
  const resp = await fetch(`${BACKEND_BASE}/loans/${encodeURIComponent(id)}/return`, {
    method: "PUT",
    headers: { Authorization: `Bearer ${token}` },
  });
  const body = await resp.text();
  return new NextResponse(body, { status: resp.status, headers: { "Content-Type": resp.headers.get("Content-Type") || "application/json" } });
}


