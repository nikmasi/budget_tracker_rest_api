USE budget_tracker;

INSERT INTO category (name, color) VALUES
('Food', '#FF5733'),
('Transport', '#3498DB'),
('Housing', '#9B59B6'),
('Entertainment', '#F1C40F'),
('Salary', '#2ECC71'),
('Health', '#E74C3C');

INSERT INTO budget (month, category_id, amount) VALUES
('2026-10', 1, 300.00),
('2026-10', 2, 100.00),
('2026-10', 3, 500.00),
('2026-10', 4, 80.00),
('2026-10', 6, 50.00),

('2026-11', 1, 320.00),
('2026-11', 2, 120.00),
('2026-11', 3, 500.00),
('2026-11', 4, 100.00),
('2026-11', 6, 60.00);


INSERT INTO `transaction`
    (type, amount, date, description, category_id)
VALUES
-- Oktobar: prihodi
('INCOME', 2500.00, '2026-10-01', 'Monthly salary', 5),
('INCOME', 150.00, '2026-10-15', 'Freelance work', 5),

-- Oktobar: troškovi
('EXPENSE', 45.50, '2026-10-02', 'Groceries', 1),
('EXPENSE', 22.00, '2026-10-04', 'Lunch', 1),
('EXPENSE', 18.00, '2026-10-05', 'Bus ticket', 2),
('EXPENSE', 350.00, '2026-10-06', 'Electricity and utilities', 3),
('EXPENSE', 25.00, '2026-10-10', 'Cinema tickets', 4),
('EXPENSE', 32.00, '2026-10-12', 'Pharmacy', 6),
('EXPENSE', 65.75, '2026-10-18', 'Weekly groceries', 1),
('EXPENSE', 12.00, '2026-10-20', 'Taxi', 2),
('EXPENSE', 40.00, '2026-10-22', 'Restaurant', 1),
('EXPENSE', 15.00, '2026-10-25', 'Streaming subscription', 4),

-- Novembar: prihodi
('INCOME', 2500.00, '2026-11-01', 'Monthly salary', 5),

-- Novembar: troskovi
('EXPENSE', 55.00, '2026-11-02', 'Groceries', 1),
('EXPENSE', 20.00, '2026-11-03', 'Public transport', 2),
('EXPENSE', 350.00, '2026-11-05', 'Utilities', 3),
('EXPENSE', 35.00, '2026-11-08', 'Concert ticket', 4),
('EXPENSE', 28.00, '2026-11-10', 'Medicine', 6);