/**
 * General-purpose string / number formatting helpers.
 */

// ─── String helpers ───────────────────────────────────────────────────────────

/**
 * Truncate a string with an ellipsis.
 * @example truncate("Hello world", 8) → "Hello..."
 */
export function truncate(str: string, maxLength: number, suffix = "..."): string {
  if (str.length <= maxLength) return str;
  return str.slice(0, maxLength - suffix.length) + suffix;
}

/**
 * Capitalize the first letter of a string.
 */
export function capitalize(str: string): string {
  if (!str) return "";
  return str.charAt(0).toUpperCase() + str.slice(1);
}

/**
 * Convert camelCase or PascalCase to "Title Case".
 * @example camelToTitle("personalInfo") → "Personal Info"
 */
export function camelToTitle(str: string): string {
  return str
    .replace(/([A-Z])/g, " $1")
    .replace(/^./, (s) => s.toUpperCase())
    .trim();
}

/**
 * Convert a string to a URL-friendly slug.
 * @example toSlug("My Resume 2024") → "my-resume-2024"
 */
export function toSlug(str: string): string {
  return str
    .toLowerCase()
    .trim()
    .replace(/[^\w\s-]/g, "")
    .replace(/[\s_-]+/g, "-")
    .replace(/^-+|-+$/g, "");
}

/**
 * Pluralize a word based on a count.
 * @example pluralize(1, "resume") → "resume"
 * @example pluralize(3, "resume") → "resumes"
 */
export function pluralize(count: number, singular: string, plural?: string): string {
  return count === 1 ? singular : (plural ?? `${singular}s`);
}

// ─── Number helpers ───────────────────────────────────────────────────────────

/**
 * Format a completion score as a percentage string.
 * @example formatScore(72) → "72%"
 */
export function formatScore(score: number): string {
  return `${Math.round(score)}%`;
}

/**
 * Clamp a number between min and max.
 */
export function clamp(value: number, min: number, max: number): number {
  return Math.min(Math.max(value, min), max);
}

// ─── File helpers ─────────────────────────────────────────────────────────────

/**
 * Format bytes to a human-readable string.
 * @example formatBytes(1536) → "1.5 KB"
 */
export function formatBytes(bytes: number): string {
  if (bytes === 0) return "0 B";
  const k = 1024;
  const sizes = ["B", "KB", "MB", "GB"];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return `${parseFloat((bytes / Math.pow(k, i)).toFixed(1))} ${sizes[i]}`;
}
