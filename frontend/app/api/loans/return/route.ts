import { NextRequest, NextResponse } from "next/server";

export async function PUT(request: NextRequest) {
  const token = request.cookies.get("token")?.value;
  if (!token) {
    return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
  }
  const { searchParams } = new URL(request.url);
  const loanId = searchParams.get("loanId");
  if (!loanId) {
    return NextResponse.json({ error: "Missing loanId" }, { status: 400 });
  }
  try {
    const resp = await fetch(`http://localhost:8083/api/loans/${encodeURIComponent(loanId)}/return`, {
      method: "PUT",
      headers: { Authorization: `Bearer ${token}` },
      cache: "no-store",
    });
    const body = await resp.text();
    return new NextResponse(body, {
      status: resp.status,
      headers: { "Content-Type": resp.headers.get("Content-Type") || "application/json" },
    });
  } catch (e) {
    return NextResponse.json({ error: "Internal Server Error" }, { status: 500 });
  }
}


