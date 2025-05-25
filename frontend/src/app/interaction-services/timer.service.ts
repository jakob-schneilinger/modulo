// timer.service.ts
import { Injectable } from '@angular/core';
import { Observable, interval } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class TimerService {
  /** Emits a number every 5 seconds */
  public readonly tick$: Observable<number> = interval(5000);

  constructor() { }
}
