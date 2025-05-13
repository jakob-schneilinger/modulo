export interface User {
  username: string;
  displayName?: string;
  email: string;
}

export interface UserUpdateDto {
  email?: string;
  displayName?: string;
  password?: string;
}
