export interface Score {
  id?: number;
  username: string;
  password?: string;
  totalScore: number;
  role?: 'PLAYER' | 'ADMIN';
}
