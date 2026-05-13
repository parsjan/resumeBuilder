"use client";

import { isAxiosError } from "axios";
import { useForm } from "react-hook-form";

import { zodResolver } from "@hookform/resolvers/zod";

import { Button } from "@/components/ui/button";
import {
  Form,
  FormControl,
  FormDescription,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { Separator } from "@/components/ui/separator";
import { registerSchema, type RegisterFormValues } from "@/validations/authSchema";
import { applyServerErrors } from "@/utils/errors";

import { useRegister } from "../hooks/useRegister";
import { OAuthButton } from "./OAuthButton";
import { PasswordInput } from "./PasswordInput";
import { PasswordStrength } from "./PasswordStrength";

export function RegisterForm() {
  const form = useForm<RegisterFormValues>({
    resolver: zodResolver(registerSchema),
    defaultValues: { fullName: "", email: "", password: "", confirmPassword: "" },
    mode: "onChange", // validate as user types (for strength indicator)
  });

  const { mutate: register, isPending } = useRegister();
  const passwordValue = form.watch("password");

  function onSubmit(values: RegisterFormValues) {
    const { confirmPassword: _omit, ...payload } = values;
    register(payload, {
      onError: (error) => {
        if (!isAxiosError(error)) return;
        if (error.response?.status === 422) {
          applyServerErrors(form.setError, error);
        }
      },
    });
  }

  return (
    <div className="space-y-5">
      {/* ── Google OAuth ── */}
      {/* <OAuthButton label="Sign up with Google" /> */}

      {/* ── Divider ── */}
      <div className="relative flex items-center">
        <Separator className="flex-1" />
        <span className="text-muted-foreground bg-background px-3 text-xs">
          or register with email
        </span>
        <Separator className="flex-1" />
      </div>

      {/* ── Registration form ── */}
      <Form {...form}>
        <form onSubmit={form.handleSubmit(onSubmit)} noValidate className="space-y-4">
          <FormField
            control={form.control}
            name="fullName"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Full name</FormLabel>
                <FormControl>
                  <Input
                    placeholder="Jane Smith"
                    autoComplete="name"
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
            name="email"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Email address</FormLabel>
                <FormControl>
                  <Input
                    type="email"
                    placeholder="you@example.com"
                    autoComplete="email"
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
                <FormLabel>Password</FormLabel>
                <FormControl>
                  <PasswordInput
                    placeholder="Create a strong password"
                    autoComplete="new-password"
                    {...field}
                  />
                </FormControl>
                <PasswordStrength password={passwordValue} />
                <FormDescription>
                  Min. 8 characters, one uppercase letter, one number.
                </FormDescription>
                <FormMessage />
              </FormItem>
            )}
          />

          <FormField
            control={form.control}
            name="confirmPassword"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Confirm password</FormLabel>
                <FormControl>
                  <PasswordInput
                    placeholder="Repeat your password"
                    autoComplete="new-password"
                    {...field}
                  />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />

          <Button type="submit" className="w-full" disabled={isPending}>
            {isPending ? "Creating account…" : "Create account"}
          </Button>
        </form>
      </Form>
    </div>
  );
}
