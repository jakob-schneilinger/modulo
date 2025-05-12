import {TestBed} from '@angular/core/testing';

import {ComponentService} from './component.service';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {ReactiveFormsModule} from '@angular/forms';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';

describe('ComponentService', () => {
  beforeEach(() => TestBed.configureTestingModule({
    imports: [RouterTestingModule, ReactiveFormsModule],
    providers: [provideHttpClient(withInterceptorsFromDi()), provideHttpClientTesting()]
  }));

  it('should be created', () => {
    const service: ComponentService = TestBed.inject(ComponentService);
    expect(service).toBeTruthy();
  });
});
