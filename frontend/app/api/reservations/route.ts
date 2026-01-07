import { NextRequest, NextResponse } from "next/server";

export async function POST(request: NextRequest) {
  const token = request.cookies.get("token")?.value;
  if (!token) return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
  const payload = await request.text();
  const resp = await fetch("http://localhost:8083/api/reservations", {
    method: "POST",
    headers: { Authorization: `Bearer ${token}`, "Content-Type": "application/json" },
    body: payload,
  });
  const body = await resp.text();
  return new NextResponse(body, {
    status: resp.status,
    headers: { "Content-Type": resp.headers.get("Content-Type") || "application/json" },
  });
}

export async function GET(request: NextRequest) {
  const token = request.cookies.get("token")?.value;
  if (!token) return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
  const scope = new URL(request.url).searchParams.get("scope");
  const url = scope === "admin"
    ? "http://localhost:8083/api/reservations"
    : "http://localhost:8083/api/reservations/me";
  const resp = await fetch(url, {
    headers: { Authorization: `Bearer ${token}` },
    cache: "no-store",
  });
  const body = await resp.text();
  return new NextResponse(body, { status: resp.status, headers: { "Content-Type": resp.headers.get("Content-Type") || "application/json" } });
}


