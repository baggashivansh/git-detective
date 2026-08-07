import { clientEnv } from "@/lib/env";
import type { ApiResponse } from "@/types/investigation/api";
import { ApiError } from "@/types/investigation/api";

const API_BASE = clientEnv.NEXT_PUBLIC_API_BASE_URL;

async function parseResponse<T>(response: Response): Promise<T> {
  const contentType = response.headers.get("content-type");
  const isJson = contentType?.includes("application/json");

  if (!response.ok) {
    if (isJson) {
      const body = (await response.json()) as { message?: string };
      throw new ApiError(body.message ?? response.statusText, response.status);
    }
    throw new ApiError(response.statusText, response.status);
  }

  if (!isJson) {
    throw new ApiError("Expected JSON response from API", response.status);
  }

  const envelope = (await response.json()) as ApiResponse<T>;

  if (!envelope.success) {
    throw new ApiError(envelope.message ?? "Request failed", response.status);
  }

  return envelope.data;
}

export async function apiGet<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`, {
    ...init,
    method: "GET",
    headers: {
      Accept: "application/json",
      ...init?.headers,
    },
  });

  return parseResponse<T>(response);
}

export async function apiPost<T>(
  path: string,
  body: unknown,
  init?: RequestInit,
): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`, {
    ...init,
    method: "POST",
    headers: {
      Accept: "application/json",
      "Content-Type": "application/json",
      ...init?.headers,
    },
    body: JSON.stringify(body),
  });

  return parseResponse<T>(response);
}
