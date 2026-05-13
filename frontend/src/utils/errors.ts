import { isAxiosError } from "axios";
import type { FieldPath, FieldValues, UseFormSetError } from "react-hook-form";

import type { ApiError } from "@/types";

/**
 * Maps 422 server validation errors onto React Hook Form fields.
 *
 * Backend error shape:
 * { errors: { email: ["Email already in use"], password: ["Too short"] } }
 */
export function applyServerErrors<T extends FieldValues>(
  setError: UseFormSetError<T>,
  error: unknown
): boolean {
  if (!isAxiosError<ApiError>(error)) return false;

  const serverErrors = error.response?.data?.errors;
  if (!serverErrors) return false;

  Object.entries(serverErrors).forEach(([field, messages]) => {
    const message = Array.isArray(messages) ? messages[0] : String(messages);
    setError(field as FieldPath<T>, { type: "server", message });
  });

  return true;
}

/**
 * Extracts a human-readable message from any error value.
 */
export function getErrorMessage(error: unknown): string {
  if (isAxiosError<ApiError>(error)) {
    return error.response?.data?.message ?? error.message;
  }
  if (error instanceof Error) return error.message;
  return "An unexpected error occurred.";
}
