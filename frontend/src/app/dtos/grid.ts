class GridVar {
  private _columns: number;
  private _rowHeight: number;

  constructor(columns: number = 8, rowHeight: number = 100) {
    this._columns = columns;
    this._rowHeight = rowHeight;

    document.documentElement.style.setProperty('--columns', `${this._columns}`);
    document.documentElement.style.setProperty('--rowHeight', `${this._rowHeight}px`);
  }

  get columns(): number {
    return this._columns;
  }

  get rowHeight(): number {
    return this._rowHeight;
  }
}

// Change Columns and RowHeight here if needed
export const gridVar = new GridVar(16, 50);
