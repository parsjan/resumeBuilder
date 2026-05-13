"use client";

import { observer } from "mobx-react-lite";
import { AlertTriangle, Loader2 } from "lucide-react";

import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { useUiStore } from "@/hooks/useStore";
import { useDeleteResume } from "../hooks/useResumes";

export const DeleteResumeDialog = observer(function DeleteResumeDialog() {
  const uiStore = useUiStore();
  const deleteResume = useDeleteResume();

  const isOpen = uiStore.isModalOpen("deleteResume");
  const data = uiStore.getModalData("deleteResume");

  function handleClose() {
    if (deleteResume.isPending) return;
    uiStore.closeModal("deleteResume");
    deleteResume.reset();
  }

  function handleConfirm() {
    if (!data) return;
    deleteResume.mutate(data.resumeId, {
      onSuccess: () => uiStore.closeModal("deleteResume"),
    });
  }

  return (
    <Dialog open={isOpen} onOpenChange={(open) => !open && handleClose()}>
      <DialogContent className="sm:max-w-sm">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <AlertTriangle className="text-destructive h-4 w-4" />
            Delete resume
          </DialogTitle>
          <DialogDescription>
            Are you sure you want to delete{" "}
            <span className="text-foreground font-medium">
              &ldquo;{data?.title || "this resume"}&rdquo;
            </span>
            ? This action cannot be undone.
          </DialogDescription>
        </DialogHeader>

        <DialogFooter>
          <Button variant="outline" onClick={handleClose} disabled={deleteResume.isPending}>
            Cancel
          </Button>
          <Button
            variant="destructive"
            onClick={handleConfirm}
            disabled={deleteResume.isPending}
          >
            {deleteResume.isPending ? (
              <>
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                Deleting…
              </>
            ) : (
              "Delete"
            )}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
});
