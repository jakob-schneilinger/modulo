import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GroupoverviewComponent } from './groupoverview.component';

describe('GroupoverviewComponent', () => {
  let component: GroupoverviewComponent;
  let fixture: ComponentFixture<GroupoverviewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GroupoverviewComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GroupoverviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
