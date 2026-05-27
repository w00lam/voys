const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '';
let csrfToken: string | null = null;
let csrfHeaderName = 'X-XSRF-TOKEN';

export type ApiError = {
  code: string;
  message: string;
  details?: Record<string, unknown>;
};

export async function apiGet<T>(path: string): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    credentials: 'include',
  });

  if (!response.ok) {
    throw await toApiError(response);
  }

  return response.json() as Promise<T>;
}

export async function apiGetBlob(path: string): Promise<Blob> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    credentials: 'include',
  });

  if (!response.ok) {
    throw await toApiError(response);
  }

  return response.blob();
}

export async function apiPost<TResponse, TBody extends object | undefined = object>(
  path: string,
  body?: TBody,
): Promise<TResponse> {
  await ensureCsrfToken();

  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
      [csrfHeaderName]: csrfToken ?? '',
    },
    body: body === undefined ? undefined : JSON.stringify(body),
  });

  if (!response.ok) {
    throw await toApiError(response);
  }

  if (response.status === 204) {
    return undefined as TResponse;
  }

  return response.json() as Promise<TResponse>;
}

export async function apiPostForm<TResponse>(path: string, body: FormData): Promise<TResponse> {
  await ensureCsrfToken();

  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      [csrfHeaderName]: csrfToken ?? '',
    },
    body,
  });

  if (!response.ok) {
    throw await toApiError(response);
  }

  return response.json() as Promise<TResponse>;
}

export async function apiPatch<TResponse, TBody extends object | undefined = object>(
  path: string,
  body?: TBody,
): Promise<TResponse> {
  await ensureCsrfToken();

  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: 'PATCH',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
      [csrfHeaderName]: csrfToken ?? '',
    },
    body: body === undefined ? undefined : JSON.stringify(body),
  });

  if (!response.ok) {
    throw await toApiError(response);
  }

  if (response.status === 204) {
    return undefined as TResponse;
  }

  return response.json() as Promise<TResponse>;
}

export function getErrorMessage(error: unknown): string {
  if (isApiError(error)) {
    return error.message;
  }

  if (error instanceof Error) {
    return error.message;
  }

  return 'Request failed.';
}

async function ensureCsrfToken(): Promise<void> {
  if (csrfToken) {
    return;
  }

  const token = await apiGet<{ headerName: string; token: string }>('/api/auth/csrf');
  csrfHeaderName = token.headerName;
  csrfToken = token.token;
}

function isApiError(error: unknown): error is ApiError {
  return typeof error === 'object' && error !== null && 'code' in error && 'message' in error;
}

async function toApiError(response: Response): Promise<ApiError> {
  try {
    return (await response.json()) as ApiError;
  } catch {
    return {
      code: `http.${response.status}`,
      message: response.statusText || 'Request failed.',
    };
  }
}

