import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Score } from '../models/score';

@Injectable({
  providedIn: 'root'
})
export class ScoreService {
  private readonly baseUrl = 'http://localhost:8080/api/score';

  constructor(private http: HttpClient) {}

  registerUsername(username: string, password: string): Observable<Score> {
    return this.http.post<Score>(`${this.baseUrl}/register`, { username, password });
  }

  login(username: string, password: string): Observable<Score> {
    return this.http.post<Score>(`${this.baseUrl}/login`, { username, password });
  }

  submitScore(username: string, bestScore: number): Observable<Score> {
    return this.http.post<Score>(`${this.baseUrl}/submit`, { username, bestScore });
  }

  leaderboard(): Observable<Score[]> {
    return this.http.get<Score[]>(`${this.baseUrl}/leaderboard`);
  }
}
