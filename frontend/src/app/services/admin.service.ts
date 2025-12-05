import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Score } from '../models/score';
import { Country } from '../models/country';

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private readonly baseUrl = 'http://localhost:8080/api/admin';

  constructor(private http: HttpClient) {}

  fullLeaderboard(username: string, password: string): Observable<Score[]> {
    return this.http.post<Score[]>(`${this.baseUrl}/leaderboard`, { username, password });
  }

  addCountry(username: string, password: string, country: Country): Observable<Country> {
    return this.http.post<Country>(`${this.baseUrl}/countries`, { username, password, country });
  }
}
