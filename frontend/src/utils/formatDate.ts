/**
 * Date formatting helpers.
 * All functions accept ISO strings, Date objects, or null/undefined.
 */

type DateInput = string | Date | null | undefined;

function toDate(input: DateInput): Date | null {
  if (!input) return null;
  const d = input instanceof Date ? input : new Date(input);
  return isNaN(d.getTime()) ? null : d;
}

/**
 * "March 2024"
 */
export function formatMonthYear(input: DateInput): string {
  const d = toDate(input);
  if (!d) return "";
  return d.toLocaleDateString("en-US", { month: "long", year: "numeric" });
}

/**
 * "Mar 2024"
 */
export function formatShortMonthYear(input: DateInput): string {
  const d = toDate(input);
  if (!d) return "";
  return d.toLocaleDateString("en-US", { month: "short", year: "numeric" });
}

/**
 * "March 2020 – March 2024"  |  "March 2020 – Present"
 */
export function formatDateRange(
  start: DateInput,
  end: DateInput,
  current = false
): string {
  const startStr = formatMonthYear(start);
  const endStr = current ? "Present" : formatMonthYear(end);
  if (!startStr) return endStr ?? "";
  return `${startStr} – ${endStr}`;
}

/**
 * "Mar 25, 2024"
 */
export function formatFullDate(input: DateInput): string {
  const d = toDate(input);
  if (!d) return "";
  return d.toLocaleDateString("en-US", { month: "short", day: "numeric", year: "numeric" });
}

/**
 * Relative time: "2 days ago", "just now", etc.
 */
export function formatRelative(input: DateInput): string {
  const d = toDate(input);
  if (!d) return "";

  const diffMs = Date.now() - d.getTime();
  const diffSec = Math.floor(diffMs / 1_000);
  const diffMin = Math.floor(diffSec / 60);
  const diffHr = Math.floor(diffMin / 60);
  const diffDay = Math.floor(diffHr / 24);

  if (diffSec < 60) return "just now";
  if (diffMin < 60) return `${diffMin} minute${diffMin === 1 ? "" : "s"} ago`;
  if (diffHr < 24) return `${diffHr} hour${diffHr === 1 ? "" : "s"} ago`;
  if (diffDay < 30) return `${diffDay} day${diffDay === 1 ? "" : "s"} ago`;
  return formatFullDate(d);
}

/**
 * ISO "YYYY-MM" used by <input type="month">
 */
export function toMonthInputValue(input: DateInput): string {
  const d = toDate(input);
  if (!d) return "";
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, "0");
  return `${y}-${m}`;
}
