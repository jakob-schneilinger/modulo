export interface UserLoginDto {
  username: string;
  password: string;
}

export interface UserCreateDto {
  username: string;
  displayName?: string;
  email: string;
  password: string;
}
