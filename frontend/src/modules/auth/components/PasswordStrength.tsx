"use client";

import { cn } from "@/utils/cn";

interface PasswordStrengthProps {
  password: string;
}

type Strength = 0 | 1 | 2 | 3 | 4;

interface StrengthMeta {
  label: string;
  color: string;
}

const STRENGTH_META: Record<Strength, StrengthMeta> = {
  0: { label: "", color: "" },
  1: { label: "Weak", color: "bg-destructive" },
  2: { label: "Fair", color: "bg-orange-400" },
  3: { label: "Good", color: "bg-yellow-400" },
  4: { label: "Strong", color: "bg-green-500" },
};

function getStrength(password: string): Strength {
  if (!password) return 0;
  let score = 0;
  if (password.length >= 8) score++;
  if (password.length >= 12) score++;
  if (/[A-Z]/.test(password)) score++;
  if (/[0-9]/.test(password)) score++;
  if (/[^A-Za-z0-9]/.test(password)) score++;
  return Math.min(4, score) as Strength;
}

export function PasswordStrength({ password }: PasswordStrengthProps) {
  const strength = getStrength(password);
  const meta = STRENGTH_META[strength];

  if (!password) return null;

  return (
    <div className="space-y-1.5">
      {/* Segmented bar */}
      <div className="flex gap-1">
        {([1, 2, 3, 4] as Strength[]).map((level) => (
          <div
            key={level}
            className={cn(
              "h-1 flex-1 rounded-full transition-colors duration-300",
              strength >= level ? meta.color : "bg-muted"
            )}
          />
        ))}
      </div>
      {meta.label && (
        <p className="text-muted-foreground text-xs">
          Password strength:{" "}
          <span
            className={cn(
              "font-medium",
              strength <= 1 && "text-destructive",
              strength === 2 && "text-orange-400",
              strength === 3 && "text-yellow-500",
              strength === 4 && "text-green-600"
            )}
          >
            {meta.label}
          </span>
        </p>
      )}
    </div>
  );
}
