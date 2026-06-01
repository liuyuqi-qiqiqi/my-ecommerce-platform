import { ref } from 'vue';

export interface Toast {
  id: number;
  message: string;
  type: 'success' | 'error' | 'warning' | 'info';
}

const toasts = ref<Toast[]>([]);
let nextId = 1;

export function useToast() {
  function show(message: string, type: Toast['type'] = 'info', durationMs = 4000) {
    const id = nextId++;
    toasts.value = [...toasts.value, { id, message, type }];
    setTimeout(() => {
      toasts.value = toasts.value.filter((t) => t.id !== id);
    }, durationMs);
  }

  function success(message: string) {
    show(message, 'success');
  }

  function error(message: string) {
    show(message, 'error', 6000);
  }

  function warning(message: string) {
    show(message, 'warning', 5000);
  }

  function info(message: string) {
    show(message, 'info');
  }

  function remove(id: number) {
    toasts.value = toasts.value.filter((t) => t.id !== id);
  }

  return { toasts, show, success, error, warning, info, remove };
}
