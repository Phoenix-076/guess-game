import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Country } from './models/country';
import { GameQuestion } from './models/game-question';
import { GameService } from './services/game.service';
import { ScoreService } from './services/score.service';
import { Score } from './models/score';
import { AdminService } from './services/admin.service';

type Status = 'idle' | 'success' | 'error' | 'info';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  country?: Country;
  options: string[] = [];
  hints: string[] = [];
  selectedOption = '';
  username = '';
  usernameReady = false;
  usernameError = '';
  password = '';
  authMode: 'login' | 'register' = 'login';
  role: 'PLAYER' | 'ADMIN' = 'PLAYER';
  level: 'easy' | 'medium' | 'hard' | 'legend' = 'easy';
  bestScore = 0;
  score = 0;
  leaderboard: Score[] = [];
  adminLeaderboard: Score[] = [];
  adminCountry: Country = { name: '', emoji: '', hints: '', difficulty: 'easy' };
  adminMessage = '';
  adminStatus: Status = 'idle';
  adminLoading = false;
  adminSaving = false;
  statusMessage = '';
  status: Status = 'idle';
  loadingCountry = false;
  checkingAnswer = false;
  view: 'welcome' | 'game' | 'leaderboard' | 'admin' = 'welcome';

  constructor(private gameService: GameService, private scoreService: ScoreService, private adminService: AdminService) { }

  ngOnInit(): void {
    this.username = localStorage.getItem('countryGuessUser') || '';
    this.password = localStorage.getItem('countryGuessPass') || '';
    const savedLevel = localStorage.getItem('countryGuessLevel') as typeof this.level;
    if (savedLevel) {
      this.level = savedLevel;
    }
    if (this.username && this.password) {
      this.resumeSession();
    }
  }

  loadQuestion(): void {
    if (!this.usernameReady || this.view !== 'game') {
      return;
    }
    this.loadingCountry = true;
    this.checkingAnswer = false;
    this.selectedOption = '';
    this.statusMessage = '';
    this.status = 'info';

    this.gameService.getRandomQuestion(4, this.level).subscribe({
      next: (question: GameQuestion) => {
        this.country = question.country;
        this.options = question.options || [];
        this.hints = this.parseHints(question.country?.hints);
        this.loadingCountry = false;
      },
      error: () => {
        this.loadingCountry = false;
        this.status = 'error';
        this.statusMessage = 'Unable to load a new country right now. Please retry.';
      }
    });
  }

  submitGuess(): void {
    if (!this.country) {
      return;
    }

    if (!this.usernameReady) {
      this.status = 'info';
      this.statusMessage = 'Save a username to play.';
      return;
    }

    if (!this.selectedOption.trim()) {
      this.status = 'info';
      this.statusMessage = 'Choose an option first.';
      return;
    }

    this.checkingAnswer = true;
    this.statusMessage = '';
    this.status = 'idle';

    this.gameService.checkAnswer(this.country.name, this.selectedOption).subscribe({
      next: (response) => {
        this.checkingAnswer = false;
        this.statusMessage = response.message;
        this.status = response.correct ? 'success' : 'error';

        if (response.correct) {
          this.applyScore(true);
          setTimeout(() => this.loadQuestion(), 1200);
        } else {
          this.applyScore(false);
        }
      },
      error: () => {
        this.checkingAnswer = false;
        this.status = 'error';
        this.statusMessage = 'Could not check your answer. Try again.';
      }
    });
  }

  private parseHints(hints?: string | null): string[] {
    if (!hints) {
      return [];
    }

    return hints
      .split(/[\n;|,]+/)
      .map((hint) => hint.trim())
      .filter(Boolean);
  }

  saveUsername(): void {
    this.username = (this.username || '').trim();
    this.password = (this.password || '').trim();
    if (!this.username) {
      this.usernameError = 'Add a username to start playing.';
      return;
    }
    if (!this.password) {
      this.usernameError = 'Password is required.';
      return;
    }

    const onSuccess = (score: Score) => {
      this.usernameReady = true;
      this.usernameError = '';
      this.bestScore = score.totalScore;
      this.applyRole(score.role);
      localStorage.setItem('countryGuessUser', this.username);
      localStorage.setItem('countryGuessPass', this.password);
      localStorage.setItem('countryGuessLevel', this.level);
      this.status = 'success';
      this.statusMessage = `Welcome, ${this.username}!`;
      this.view = this.isAdmin ? 'admin' : 'welcome';
      this.score = 0;
      if (this.isAdmin) {
        this.loadAdminLeaderboard();
      } else {
        this.refreshLeaderboard();
      }
    };

    if (this.authMode === 'register') {
      this.scoreService.registerUsername(this.username, this.password).subscribe({
        next: onSuccess,
        error: (err) => {
          this.usernameReady = false;
          if (err.status === 409) {
            this.usernameError = 'That username is already taken.';
          } else {
            this.usernameError = 'Could not register. Try again.';
          }
        }
      });
    } else {
      this.scoreService.login(this.username, this.password).subscribe({
        next: onSuccess,
        error: (err) => {
          this.usernameReady = false;
          if (err.status === 401) {
            this.usernameError = 'Invalid username or password.';
          } else {
            this.usernameError = 'Could not log in. Try again.';
          }
        }
      });
    }
  }

  get isAdmin(): boolean {
    return this.role === 'ADMIN';
  }

  private resumeSession(): void {
    this.scoreService.login(this.username, this.password).subscribe({
      next: (user) => {
        this.usernameReady = true;
        this.usernameError = '';
        this.bestScore = user.totalScore;
        this.applyRole(user.role);
        this.view = this.isAdmin ? 'admin' : 'welcome';
        if (this.isAdmin) {
          this.loadAdminLeaderboard();
        } else {
          this.refreshLeaderboard();
        }
      },
      error: () => {
        this.handleSessionExpired();
      }
    });
  }

  private handleSessionExpired(): void {
    this.usernameReady = false;
    this.usernameError = 'Session expired. Please sign in again.';
    this.bestScore = 0;
    this.score = 0;
    this.clearSavedSession();
  }

  private clearSavedSession(): void {
    localStorage.removeItem('countryGuessUser');
    localStorage.removeItem('countryGuessPass');
    localStorage.removeItem('countryGuessLevel');
    localStorage.removeItem('countryGuessRole');
  }

  private applyRole(role?: string | null): void {
    this.role = role && role.toUpperCase() === 'ADMIN' ? 'ADMIN' : 'PLAYER';
    localStorage.setItem('countryGuessRole', this.role);
    if (!this.isAdmin) {
      this.adminLeaderboard = [];
      this.adminMessage = '';
      this.adminStatus = 'idle';
      if (this.view === 'admin') {
        this.view = 'welcome';
      }
    } else {
      this.view = 'admin';
    }
  }

  private applyScore(correct: boolean): void {
    if (!this.username.trim() || this.isAdmin) {
      return;
    }
    this.score = Math.max(0, this.score + this.pointsFor(correct));
    const currentRun = this.score;
    this.scoreService.submitScore(this.username, currentRun).subscribe({
      next: (score) => {
        this.bestScore = score.totalScore;
        this.refreshLeaderboard();
      },
      error: () => {
        // best-effort; avoid blocking gameplay
      }
    });
  }

  private pointsFor(correct: boolean): number {
    if (correct) {
      return this.level === 'medium' ? 2 : this.level === 'hard' ? 4 : this.level === 'legend' ? 8 : 1;
    }
    return this.level === 'hard' || this.level === 'legend' ? -2 : 0;
  }

  refreshLeaderboard(): void {
    this.scoreService.leaderboard().subscribe({
      next: (rows) => {
        this.leaderboard = rows;
        const mine = rows.find((r) => r.username.toLowerCase() === this.username.toLowerCase());
        if (mine) {
          this.bestScore = mine.totalScore;
        }
      },
      error: () => {
        this.leaderboard = [];
      }
    });
  }

  loadAdminLeaderboard(): void {
    if (!this.isAdmin || !this.username || !this.password) {
      return;
    }
    this.adminLoading = true;
    this.adminStatus = 'idle';
    this.adminMessage = '';
    this.adminService.fullLeaderboard(this.username, this.password).subscribe({
      next: (rows) => {
        this.adminLeaderboard = rows || [];
        this.adminLoading = false;
      },
      error: () => {
        this.adminLoading = false;
        this.adminLeaderboard = [];
        this.adminStatus = 'error';
        this.adminMessage = 'Unable to load the full leaderboard.';
      }
    });
  }

  addAdminCountry(): void {
    if (!this.isAdmin) {
      return;
    }
    const payload: Country = {
      name: (this.adminCountry.name || '').trim(),
      emoji: (this.adminCountry.emoji || '').trim(),
      hints: (this.adminCountry.hints || '').trim(),
      difficulty: this.adminCountry.difficulty || this.level
    };

    if (!payload.name || !payload.emoji) {
      this.adminStatus = 'info';
      this.adminMessage = 'Name and emoji are required.';
      return;
    }

    this.adminSaving = true;
    this.adminStatus = 'info';
    this.adminMessage = 'Saving question...';

    this.adminService.addCountry(this.username, this.password, payload).subscribe({
      next: (country) => {
        this.adminSaving = false;
        this.adminStatus = 'success';
        this.adminMessage = `Added ${country.name} to ${country.difficulty || payload.difficulty} difficulty.`;
        this.adminCountry = { name: '', emoji: '', hints: '', difficulty: country.difficulty || payload.difficulty || 'easy' };
      },
      error: (err) => {
        this.adminSaving = false;
        this.adminStatus = 'error';
        if (err.status === 409) {
          this.adminMessage = 'That country already exists.';
        } else if (err.status === 401) {
          this.adminMessage = 'Admin access required. Please log in again.';
        } else {
          this.adminMessage = 'Could not save the question.';
        }
      }
    });
  }

  switchAccount(): void {
    this.usernameReady = false;
    this.username = '';
    this.password = '';
    this.bestScore = 0;
    this.score = 0;
    this.level = 'easy';
    this.role = 'PLAYER';
    this.adminLeaderboard = [];
    this.adminCountry = { name: '', emoji: '', hints: '', difficulty: 'easy' };
    this.adminMessage = '';
    this.adminStatus = 'idle';
    this.adminLoading = false;
    this.adminSaving = false;
    this.clearSavedSession();
    this.statusMessage = '';
    this.status = 'idle';
    this.view = 'welcome';
  }

  selectLevel(newLevel: typeof this.level): void {
    this.level = newLevel;
    this.score = 0;
    localStorage.setItem('countryGuessLevel', this.level);
  }

  startGame(): void {
    if (this.isAdmin) {
      this.status = 'info';
      this.statusMessage = 'Admin accounts are for managing questions and scores.';
      return;
    }
    this.score = 0;
    this.view = 'game';
    this.loadQuestion();
  }

  openLeaderboard(): void {
    if (this.isAdmin) {
      this.view = 'admin';
      return;
    }
    this.refreshLeaderboard();
    this.view = 'leaderboard';
  }

  backFromLeaderboard(): void {
    if (this.isAdmin) {
      this.view = 'admin';
      return;
    }
    this.view = 'welcome';
  }

  backToMenu(): void {
    this.score = 0;
    this.view = this.isAdmin ? 'admin' : 'welcome';
  }
}
