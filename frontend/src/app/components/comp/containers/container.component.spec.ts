import { ComponentFixture, TestBed } from "@angular/core/testing";

import { ContainerComponent } from "./container.component";

describe("ContainerComponent", () => {
  let component: ContainerComponent<any>;
  let fixture: ComponentFixture<ContainerComponent<any>>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ContainerComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(ContainerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });
});
