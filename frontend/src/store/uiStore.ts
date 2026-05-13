import { action, makeObservable, observable } from "mobx";

import type { RootStore } from "./rootStore";

// ─── Modal Registry ───────────────────────────────────────────────────────────

interface ModalDataMap {
  createResume: null;
  deleteResume: { resumeId: string; title: string };
  aiSuggestion: { sectionType: string; itemIndex: number; bulletIndex: number };
  generateResume: null;
}

type ModalKey = keyof ModalDataMap;

type ModalState = {
  [K in ModalKey]: {
    open: boolean;
    data: ModalDataMap[K] | null;
  };
};

function buildModalState(): ModalState {
  return {
    createResume: { open: false, data: null },
    deleteResume: { open: false, data: null },
    aiSuggestion: { open: false, data: null },
    generateResume: { open: false, data: null },
  };
}

// ─── UiStore ─────────────────────────────────────────────────────────────────

export class UiStore {
  modals: ModalState = buildModalState();
  sidebarOpen = true;
  globalLoading = false;

  constructor(private rootStore: RootStore) {
    makeObservable(this, {
      modals: observable,
      sidebarOpen: observable,
      globalLoading: observable,
      openModal: action,
      closeModal: action,
      toggleSidebar: action,
      setGlobalLoading: action,
      reset: action,
    });
  }

  // ─── Modals ──────────────────────────────────────────────────────────────

  openModal<K extends ModalKey>(key: K, data?: ModalDataMap[K]) {
    this.modals[key].open = true;
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    this.modals[key].data = (data ?? null) as any;
  }

  closeModal(key: ModalKey) {
    this.modals[key].open = false;
    this.modals[key].data = null;
  }

  isModalOpen(key: ModalKey): boolean {
    return this.modals[key].open;
  }

  getModalData<K extends ModalKey>(key: K): ModalDataMap[K] | null {
    return this.modals[key].data as ModalDataMap[K] | null;
  }

  // ─── Layout ──────────────────────────────────────────────────────────────

  toggleSidebar() {
    this.sidebarOpen = !this.sidebarOpen;
  }

  setGlobalLoading(loading: boolean) {
    this.globalLoading = loading;
  }

  // ─── Lifecycle ───────────────────────────────────────────────────────────

  reset() {
    this.modals = buildModalState();
    this.globalLoading = false;
    // Preserve sidebarOpen — it's a layout preference, not auth state
  }
}
