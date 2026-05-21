import { apiGet, apiPost } from '../../shared/api/client';

export type CurrentUser = {
  id: string;
  email: string;
  displayName: string;
};

export type SignUpRequest = {
  email: string;
  password: string;
  displayName: string;
};

export type LoginRequest = {
  email: string;
  password: string;
};

export function getCurrentUser(): Promise<CurrentUser> {
  return apiGet<CurrentUser>('/api/me');
}

export function signUp(request: SignUpRequest): Promise<CurrentUser> {
  return apiPost<CurrentUser, SignUpRequest>('/api/auth/signup', request);
}

export function login(request: LoginRequest): Promise<CurrentUser> {
  return apiPost<CurrentUser, LoginRequest>('/api/auth/login', request);
}

export function logout(): Promise<void> {
  return apiPost<void, undefined>('/api/auth/logout');
}
