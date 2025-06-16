import { Component, ElementRef, OnInit, ViewChild } from "@angular/core";
import { BaseComponent } from "../base/base.component";
import {Calendar, CalendarEntry} from "src/app/dtos/component";
import { CommonModule } from "@angular/common";
import ComponentFactory from "src/app/global/ComponentFactory";
import type { ContextMenuAction } from "../context-menu/context-menu.component";

@Component({
  selector: "app-calendar-component",
  templateUrl: "./calendar.component.html",
  styleUrls: ["./calendar.component.scss", "../base/base.component.scss"],
  standalone: false,
})
export class CalendarComponent extends BaseComponent<Calendar> implements OnInit {
  @ViewChild("content") content: ElementRef<HTMLImageElement>;
  @ViewChild("input") input: ElementRef<HTMLInputElement>;

  actions: ContextMenuAction[] = [
    {label: "Change Source", action: () => this.self.entries = []},
    {label: "Enable Edit Mode", action: () => this.enableEditMode()},
    {label: "Delete Calendar", action: () => this.deleteComponent()},
    {label: "Clear Calendar", action: () => this.clearCalendar()},
  ];

  inputActive: boolean = false;

  clearCalendar():void {
    this.componentService.clearCalendar(this.self).subscribe({
      next: value => this.self.entries = [],
      error :(e) => console.error(e)
    });
  }

  refresh():void{
    this.componentService.refreshCalendar(this.self).subscribe({
      next: value => this.self.entries = value.entries ?? this.self.entries,
      error: (e) => console.error(e)
    });
  }

  setFile(calendarFile: File) {
    if (!calendarFile || this.readonlyMode) return;

    this.componentService.setCalendarFile(this.self, calendarFile).subscribe({
      next: (value) => {
        this.inputActive = false;
        this.self.entries = value.entries;
      },
      error: (e) => console.error(e),
    });
  }

  setUrl(url: string) {
    if (!url || this.readonlyMode) return;
    this.componentService.setCalendarUrl(this.self, url).subscribe({
      next: (value) => {
        this.inputActive = false;
        this.self.entries = value.entries;
        },
      error: (e) => console.error(e),
    });
  }

  focusFileInput() {
    this.input.nativeElement.click();
  }

  filterStartDate?: string;
  filterEndDate?: string;

  get entries(): CalendarEntry[] {
    const from = this.filterStartDate ? new Date(this.filterStartDate) : undefined;
    const to = this.filterEndDate ? new Date(this.filterEndDate) : undefined;

    from?.setHours(0, 0, 0, 0);
    to?.setHours(0, 0, 0, 0);

    return this.self.entries.filter(e => {
      const start = new Date(e.startDate);
      const end = new Date(e.startDate);
      if (from && from > start) return false;
      if (to && to < end) return false;
      return true;
    });
  }

  formatDate(start: Date, end: Date): string {
    const sDate = new Date(start);
    const eDate = new Date(end);

    const now = new Date();
    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    const tomorrow = new Date(today);
    tomorrow.setDate(today.getDate() + 1);
    const yesterday = new Date(today);
    yesterday.setDate(today.getDate() - 1);

    const dateString = (d: Date): string => {
      if (this.isSameDay(d, today)) return 'today';
      if (this.isSameDay(d, tomorrow)) return 'tomorrow';
      if (this.isSameDay(d, yesterday)) return 'yesterday';
      return this.dateFormat(d);
    };

    if (this.isSameDay(sDate, eDate)) return dateString(sDate);
    return `${dateString(sDate)} - ${dateString(eDate)}`;
  }

  private dateFormat(date: Date) {
    const day = date.getDate().toString().padStart(2, '0');
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const year = date.getFullYear();
    return `${day}.${month}.${year}`;
  }

  private isSameDay(d1: Date, d2: Date) {
    return d1.getFullYear() === d2.getFullYear()
      && d1.getMonth() === d2.getMonth()
      && d1.getDate() === d2.getDate();
  }

  ngOnInit(): void {
    const date = new Date();
    this.filterStartDate = date.toISOString().split("T")[0];
  }

}
ComponentFactory.addComponentType("calendar", CalendarComponent);
