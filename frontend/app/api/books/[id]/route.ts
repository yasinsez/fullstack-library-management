import { NextRequest, NextResponse } from "next/server";

export async function PUT(request: NextRequest, context: unknown) {
  const paramsVal = (context as { params?: unknown }).params;
  let id: string | null = null;
  if (paramsVal && typeof paramsVal === "object") {
    const maybeId = (paramsVal as Record<string, unknown>).id;
    if (typeof maybeId === "string") id = maybeId;
  }
  if (!id) {
    return NextResponse.json({ error: "Invalid id" }, { status: 400 });
  }
  const token = request.cookies.get("token")?.value;
  if (!token) return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
  const payload = await request.text();
  const resp = await fetch(`http://localhost:8083/api/books/${encodeURIComponent(id)}`, {
    method: "PUT",
    headers: { Authorization: `Bearer ${token}`, "Content-Type": "application/json" },
    body: payload,
  });
  const body = await resp.text();
  return new NextResponse(body, {
    status: resp.status,
    headers: { "Content-Type": resp.headers.get("Content-Type") || "application/json" },
  });
}

export async function DELETE(request: NextRequest, context: unknown) {
  const paramsVal = (context as { params?: unknown }).params;
  let id: string | null = null;
  if (paramsVal && typeof paramsVal === "object") {
    const maybeId = (paramsVal as Record<string, unknown>).id;
    if (typeof maybeId === "string") id = maybeId;
  }
  if (!id) {
    return NextResponse.json({ error: "Invalid id" }, { status: 400 });
  }
  const token = request.cookies.get("token")?.value;
  if (!token) return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
  const resp = await fetch(`http://localhost:8083/api/books/${encodeURIComponent(id)}`, {
    method: "DELETE",
    headers: { Authorization: `Bearer ${token}` },
  });
  return new NextResponse(null, { status: resp.status });
}


