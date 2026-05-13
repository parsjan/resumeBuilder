"use client";

import { isAxiosError } from "axios";
import Link from "next/link";
import { useForm } from "react-hook-form";

import { zodResolver } from "@hookform/resolvers/zod";

import { Button } from "@/components/ui/button";
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { Separator } from "@/components/ui/separator";
import { loginSchema, type LoginFormValues } from "@/validations/authSchema";
import { applyServerErrors, getErrorMessage } from "@/utils/errors";

import { useLogin } from "../hooks/useLogin";
import { OAuthButton } from "./OAuthButton";
import { PasswordInput } from "./PasswordInput";

export function LoginForm() {
  const form = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: { email: "", password: "" },
  });

  const { mutate: login, isPending } = useLogin();

  function onSubmit(values: LoginFormValues) {
    login(values, {
      onError: (error) => {
        if (!isAxiosError(error)) return;

        const status = error.response?.status;

        // 422 — map server field errors onto the form
        if (status === 422) {
          applyServerErrors(form.setError, error);
          return;
        }

        // 401 / 400 — bad credentials
        if (status === 401 || status === 400) {
          form.setError("password", {
            type: "server",
            message: getErrorMessage(error),
          });
          return;
        }
      },
    });
  }

  return (
    <div className="space-y-5">
      {/* ── Google OAuth ── */}
      {/* <OAuthButton label="Continue with Google" /> */}

      {/* ── Divider ── */}
      <div className="relative flex items-center">
        <Separator className="flex-1" />
        <span className="text-muted-foreground bg-background px-3 text-xs">
          or continue with email
        </span>
        <Separator className="flex-1" />
      </div>

      {/* ── Email / password form ── */}
      <Form {...form}>
        <form onSubmit={form.handleSubmit(onSubmit)} noValidate className="space-y-4">
          <FormField
            control={form.control}
            name="email"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Email address</FormLabel>
                <FormControl>
                  <Input
                    type="email"
                    placeholder="you@example.com"
                    autoComplete="email"
                    autoFocus
                    {...field}
                  />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />

          <FormField
            control={form.control}
            name="password"
            render={({ field }) => (
              <FormItem>
                <div className="flex items-center justify-between">
                  <FormLabel>Password</FormLabel>
                  <Link
                    href="/forgot-password"
                    className="text-muted-foreground hover:text-foreground text-xs underline-offset-4 transition-colors hover:underline"
                  >
                    Forgot password?
                  </Link>
                </div>
                <FormControl>
                  <PasswordInput
                    placeholder="Enter your password"
                    autoComplete="current-password"
                    {...field}
                  />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />

          <Button type="submit" className="w-full" disabled={isPending}>
            {isPending ? "Signing in…" : "Sign in"}
          </Button>
        </form>
      </Form>
    </div>
  );
}
