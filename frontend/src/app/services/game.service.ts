import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Country } from '../models/country';
import { GameQuestion } from '../models/game-question';

interface CheckResponse {
  correct: boolean;
  message: string;
}

@Injectable({
  providedIn: 'root'
})
export class GameService {
  private readonly baseUrl = 'http://localhost:8080/api/game';

  constructor(private http: HttpClient) {}

  getRandomQuestion(options = 4, level = 'easy'): Observable<GameQuestion> {
    return this.http.get<GameQuestion>(`${this.baseUrl}/question`, {
      params: { options, level }
    });
  }

  checkAnswer(countryName: string, guess: string): Observable<CheckResponse> {
    return this.http.post<CheckResponse>(`${this.baseUrl}/check`, {
      countryName,
      guess
    });
  }
}
