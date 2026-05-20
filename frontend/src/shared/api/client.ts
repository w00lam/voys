const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '';

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

